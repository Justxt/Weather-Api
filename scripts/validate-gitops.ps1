[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot

function Assert-Contains {
    param(
        [Parameter(Mandatory)][string]$Path,
        [Parameter(Mandatory)][string]$Pattern,
        [Parameter(Mandatory)][string]$Message
    )

    $content = Get-Content -LiteralPath $Path -Raw
    if ($content -notmatch $Pattern) {
        throw $Message
    }
}

$application = Join-Path $root "argocd/application.yaml"
$kustomization = Join-Path $root "k8s/base/kustomization.yaml"
$workflow = Join-Path $root ".github/workflows/devsecops.yml"
$installer = Join-Path $root "scripts/setup-argocd.ps1"

foreach ($path in @($application, $kustomization, $workflow, $installer)) {
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        throw "Falta el archivo requerido: $path"
    }
}

Assert-Contains $application "(?m)^\s+selfHeal:\s+true\s*$" "Argo CD debe tener selfHeal habilitado."
Assert-Contains $application "(?m)^\s+prune:\s+true\s*$" "Argo CD debe tener prune habilitado."
Assert-Contains $application "(?m)^\s+targetRevision:\s+main\s*$" "Argo CD debe observar main."
Assert-Contains $application "(?m)^\s+path:\s+k8s/base\s*$" "Argo CD debe renderizar k8s/base."
Assert-Contains $kustomization "(?m)^\s+digest:\s+sha256:[0-9a-f]{64}\s*$" "Kustomize debe fijar una imagen por digest SHA-256."
Assert-Contains $workflow "Update GitOps image digest" "El workflow debe promover el digest en Git."
Assert-Contains $workflow "setup-argocd\.ps1" "El workflow debe instalar o validar Argo CD."
Assert-Contains $workflow "EXPECTED_REVISION" "El workflow debe validar la revisión promovida."
Assert-Contains $workflow "EXPECTED_IMAGE" "El workflow debe validar la imagen desplegada."
Assert-Contains $workflow "argocd-evidence" "El workflow debe generar evidencia de Argo CD."
Assert-Contains $installer "v3\.4\.5" "La versión de Argo CD debe estar fijada."
Assert-Contains $installer "cdf6758b489d25641c2a1fd835642543aaa64fe530867d0136a83ddf3dafe456" "El manifiesto de Argo CD debe verificarse por SHA-256."

$imperativeFiles = @($workflow, (Join-Path $root "scripts/deploy-local.ps1"))
foreach ($path in $imperativeFiles) {
    $content = Get-Content -LiteralPath $path -Raw
    if ($content -match "(?im)kubectl\s+.*(?:set\s+image|rollout\s+restart)") {
        throw "Se detectó un despliegue imperativo que compite con Argo CD en $path"
    }
}

Write-Host "Contrato GitOps válido: Argo CD es el único reconciliador del Deployment."
