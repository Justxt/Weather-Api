[CmdletBinding()]
param(
    [string]$Image = "weather-api:local",
    [string]$Profile = "minikube"
)

$ErrorActionPreference = "Stop"
$repositoryRoot = Split-Path -Parent $PSScriptRoot

function Assert-LastExitCode {
    param(
        [Parameter(Mandatory)]
        [string]$Message
    )

    if ($LASTEXITCODE -ne 0) {
        throw $Message
    }
}

Push-Location $repositoryRoot
try {
    foreach ($command in @("docker", "minikube", "kubectl")) {
        if (-not (Get-Command $command -ErrorAction SilentlyContinue)) {
            throw "No se encontro '$command' en PATH. Instalalo antes de continuar."
        }
    }

    minikube status --profile $Profile *> $null
    Assert-LastExitCode "Minikube no esta iniciado. Ejecuta scripts/setup-minikube.ps1."

    docker build --tag $Image .
    Assert-LastExitCode "No se pudo construir la imagen '$Image'."

    minikube image load $Image --profile $Profile
    Assert-LastExitCode "No se pudo cargar la imagen '$Image' en Minikube."

    kubectl config use-context $Profile
    Assert-LastExitCode "No se pudo seleccionar el contexto '$Profile'."

    kubectl kustomize k8s/base | kubectl apply -f -
    Assert-LastExitCode "No se pudieron aplicar los manifiestos de Kubernetes."

    kubectl -n weather-api set image deployment/weather-api "weather-api=$Image"
    Assert-LastExitCode "No se pudo actualizar la imagen del Deployment."

    kubectl -n weather-api rollout restart deployment/weather-api
    Assert-LastExitCode "No se pudo reiniciar el Deployment."

    kubectl -n weather-api rollout status deployment/weather-api --timeout=240s
    Assert-LastExitCode "El Deployment no quedo disponible dentro del tiempo esperado."

    $portForward = Start-Job -ScriptBlock {
        kubectl -n weather-api port-forward service/weather-api 18080:80
    }

    try {
        $response = $null
        for ($attempt = 1; $attempt -le 30; $attempt++) {
            try {
                $response = Invoke-RestMethod http://127.0.0.1:18080/api/health
                break
            } catch {
                Start-Sleep -Seconds 2
            }
        }

        if ($null -eq $response -or $response.status -ne "UP") {
            throw "El smoke test no obtuvo status UP."
        }

        kubectl -n weather-api get deployment,pods,service -o wide
        Write-Host "Health: $($response | ConvertTo-Json -Compress)"
    } finally {
        Stop-Job $portForward -ErrorAction SilentlyContinue
        Remove-Job $portForward -Force -ErrorAction SilentlyContinue
    }
} finally {
    Pop-Location
}
