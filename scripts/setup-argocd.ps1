[CmdletBinding()]
param(
    [string]$Profile = "minikube"
)

$ErrorActionPreference = "Stop"
$version = "v3.4.5"
$expectedSha256 = "cdf6758b489d25641c2a1fd835642543aaa64fe530867d0136a83ddf3dafe456"
$manifestUri = "https://raw.githubusercontent.com/argoproj/argo-cd/$version/manifests/install.yaml"
$manifestPath = Join-Path $env:TEMP "argocd-$version-install.yaml"

function Assert-LastExitCode {
    param([Parameter(Mandatory)][string]$Message)

    if ($LASTEXITCODE -ne 0) {
        throw $Message
    }
}

foreach ($command in @("kubectl", "minikube")) {
    if (-not (Get-Command $command -ErrorAction SilentlyContinue)) {
        throw "No se encontró '$command' en PATH."
    }
}

minikube status --profile $Profile *> $null
Assert-LastExitCode "Minikube no está disponible en el perfil '$Profile'."

kubectl config use-context $Profile | Out-Null
Assert-LastExitCode "No se pudo seleccionar el contexto '$Profile'."

Invoke-WebRequest -Uri $manifestUri -OutFile $manifestPath
$actualSha256 = (Get-FileHash -LiteralPath $manifestPath -Algorithm SHA256).Hash.ToLowerInvariant()
if ($actualSha256 -ne $expectedSha256) {
    throw "El manifiesto de Argo CD no superó la verificación SHA-256. Esperado: $expectedSha256. Obtenido: $actualSha256."
}

kubectl create namespace argocd --dry-run=client -o yaml | kubectl apply -f - | Out-Null
Assert-LastExitCode "No se pudo crear el namespace argocd."

kubectl apply --server-side --force-conflicts -n argocd -f $manifestPath | Out-Null
Assert-LastExitCode "No se pudo instalar Argo CD $version."

kubectl wait --for=condition=Established crd/applications.argoproj.io --timeout=180s | Out-Null
Assert-LastExitCode "El CRD applications.argoproj.io no quedó disponible."

$deployments = @(kubectl -n argocd get deployments -o jsonpath='{.items[*].metadata.name}') -split '\s+' | Where-Object { $_ }
foreach ($deployment in $deployments) {
    kubectl -n argocd rollout status "deployment/$deployment" --timeout=300s | Out-Null
    Assert-LastExitCode "El Deployment de Argo CD '$deployment' no quedó disponible."
}

$statefulSets = @(kubectl -n argocd get statefulsets -o jsonpath='{.items[*].metadata.name}') -split '\s+' | Where-Object { $_ }
foreach ($statefulSet in $statefulSets) {
    kubectl -n argocd rollout status "statefulset/$statefulSet" --timeout=300s | Out-Null
    Assert-LastExitCode "El StatefulSet de Argo CD '$statefulSet' no quedó disponible."
}

Write-Host "Argo CD $version está instalado y operativo en el namespace argocd."
