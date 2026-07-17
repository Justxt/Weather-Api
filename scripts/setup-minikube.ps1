[CmdletBinding()]
param(
    [ValidateRange(2, 16)]
    [int]$Cpus = 2,

    [ValidateRange(3072, 32768)]
    [int]$Memory = 4096,

    [string]$Profile = "minikube"
)

$ErrorActionPreference = "Stop"

function Assert-LastExitCode {
    param(
        [Parameter(Mandatory)]
        [string]$Message
    )

    if ($LASTEXITCODE -ne 0) {
        throw $Message
    }
}

foreach ($command in @("docker", "minikube", "kubectl")) {
    if (-not (Get-Command $command -ErrorAction SilentlyContinue)) {
        throw "No se encontro '$command' en PATH. Instalalo antes de continuar."
    }
}

docker info *> $null
Assert-LastExitCode "Docker Desktop no esta iniciado."

minikube status --profile $Profile *> $null
if ($LASTEXITCODE -ne 0) {
    minikube start `
        --profile $Profile `
        --driver docker `
        --cpus $Cpus `
        --memory $Memory
    Assert-LastExitCode "Minikube no pudo iniciar el perfil '$Profile'."
}

kubectl config use-context $Profile
Assert-LastExitCode "No se pudo seleccionar el contexto '$Profile'."

kubectl create namespace weather-api --dry-run=client -o yaml | kubectl apply -f -
Assert-LastExitCode "No se pudo crear o actualizar el namespace 'weather-api'."

Write-Host "Minikube esta listo en el perfil '$Profile'."
