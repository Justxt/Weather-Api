[CmdletBinding()]
param(
    [string]$Profile = "minikube",

    [ValidateRange(120, 1800)]
    [int]$TimeoutSeconds = 600
)

$ErrorActionPreference = "Stop"
$repositoryRoot = Split-Path -Parent $PSScriptRoot
$evidenceDirectory = Join-Path $repositoryRoot "argocd-evidence"

function Assert-LastExitCode {
    param([Parameter(Mandatory)][string]$Message)

    if ($LASTEXITCODE -ne 0) {
        throw $Message
    }
}

Push-Location $repositoryRoot
try {
    foreach ($command in @("git", "minikube", "kubectl")) {
        if (-not (Get-Command $command -ErrorAction SilentlyContinue)) {
            throw "No se encontró '$command' en PATH."
        }
    }

    minikube status --profile $Profile *> $null
    Assert-LastExitCode "Minikube no está iniciado. Ejecuta scripts/setup-minikube.ps1."

    & (Join-Path $PSScriptRoot "setup-argocd.ps1") -Profile $Profile

    git fetch origin main --quiet
    Assert-LastExitCode "No se pudo consultar la revisión actual de main."
    $expectedRevision = (git rev-parse origin/main).Trim()

    $remoteKustomization = (git show origin/main:k8s/base/kustomization.yaml) -join "`n"
    Assert-LastExitCode "No se pudo leer k8s/base/kustomization.yaml desde origin/main."
    $newName = ([regex]::Match($remoteKustomization, '(?m)^\s+newName:\s+(\S+)\s*$')).Groups[1].Value
    $digest = ([regex]::Match($remoteKustomization, '(?m)^\s+digest:\s+(sha256:[0-9a-f]{64})\s*$')).Groups[1].Value
    if ([string]::IsNullOrWhiteSpace($newName) -or [string]::IsNullOrWhiteSpace($digest)) {
        throw "No se pudo obtener la imagen inmutable declarada en origin/main."
    }
    $expectedImage = "$newName@$digest"

    kubectl apply -f argocd/application.yaml | Out-Null
    Assert-LastExitCode "No se pudo registrar la Application weather-api."
    kubectl -n argocd annotate application weather-api argocd.argoproj.io/refresh=hard --overwrite | Out-Null
    Assert-LastExitCode "No se pudo solicitar el refresh de Argo CD."

    New-Item -ItemType Directory -Force -Path $evidenceDirectory | Out-Null
    $attempts = [math]::Ceiling($TimeoutSeconds / 10)
    $lastStatus = ""
    for ($attempt = 1; $attempt -le $attempts; $attempt++) {
        $application = kubectl -n argocd get application weather-api -o json | ConvertFrom-Json
        $sync = $application.status.sync.status
        $health = $application.status.health.status
        $revision = $application.status.sync.revision
        $lastStatus = "sync=$sync health=$health revision=$revision expected=$expectedRevision"
        $lastStatus | Tee-Object -FilePath (Join-Path $evidenceDirectory "sync-status.log") -Append
        if ($sync -eq "Synced" -and $health -eq "Healthy" -and $revision -eq $expectedRevision) {
            break
        }
        Start-Sleep -Seconds 10
    }

    if ($sync -ne "Synced" -or $health -ne "Healthy" -or $revision -ne $expectedRevision) {
        throw "Argo CD no alcanzó el estado esperado. Último estado: $lastStatus"
    }

    $deployedImage = kubectl -n weather-api get deployment weather-api -o jsonpath='{.spec.template.spec.containers[0].image}'
    if ($deployedImage -ne $expectedImage) {
        throw "La imagen desplegada no coincide con Git. Esperada: $expectedImage. Desplegada: $deployedImage"
    }
    kubectl -n weather-api rollout status deployment/weather-api --timeout=240s
    Assert-LastExitCode "El Deployment no quedó disponible."

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

        $request = @{
            city = "Quito"
            date = (Get-Date).ToUniversalTime().AddDays(3).ToString("yyyy-MM-dd")
        } | ConvertTo-Json
        $risk = $null
        $lastFunctionalError = ""
        for ($attempt = 1; $attempt -le 6; $attempt++) {
            try {
                $risk = Invoke-RestMethod -Method Post -Uri http://127.0.0.1:18080/api/flight-cancellation-risk -ContentType "application/json" -Body $request
                break
            } catch {
                $lastFunctionalError = $_.Exception.Message
                Write-Warning "Functional smoke attempt $attempt failed: $lastFunctionalError"
                Start-Sleep -Seconds 5
            }
        }
        if ([string]::IsNullOrWhiteSpace($risk.riskLevel)) {
            throw "El smoke test funcional no obtuvo riskLevel después de 6 intentos. Último error: $lastFunctionalError"
        }

        "expectedRevision=$expectedRevision" | Out-File (Join-Path $evidenceDirectory "expected-state.txt")
        "expectedImage=$expectedImage" | Out-File -Append (Join-Path $evidenceDirectory "expected-state.txt")
        "deployedImage=$deployedImage" | Out-File -Append (Join-Path $evidenceDirectory "expected-state.txt")
        "Health: $($response | ConvertTo-Json -Compress)" | Out-File (Join-Path $evidenceDirectory "smoke-tests.txt")
        "Functional: $($risk | ConvertTo-Json -Compress)" | Out-File -Append (Join-Path $evidenceDirectory "smoke-tests.txt")
        kubectl -n argocd get application weather-api -o yaml | Out-File (Join-Path $evidenceDirectory "application.yaml")
        kubectl -n weather-api get deployment,pods,service -o wide | Out-File (Join-Path $evidenceDirectory "workloads.txt")

        Write-Host "GitOps validado: Synced, Healthy, digest correcto y smoke tests aprobados."
    } finally {
        Stop-Job $portForward -ErrorAction SilentlyContinue
        Remove-Job $portForward -Force -ErrorAction SilentlyContinue
    }
} finally {
    Pop-Location
}
