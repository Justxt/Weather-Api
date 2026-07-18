# Integración obligatoria de Argo CD — Plan de implementación

**Objetivo:** convertir el despliegue de Weather-Api en un flujo GitOps verificable donde Argo CD sea el único reconciliador de los manifiestos Kubernetes.

**Arquitectura:** GitHub Actions construye, analiza, publica, firma y promueve un digest inmutable en Git. Argo CD observa ese commit, sincroniza Minikube y el runner autoalojado valida revisión, salud, digest y funcionamiento.

**Tecnologías:** GitHub Actions, GHCR, Cosign, Kustomize, Argo CD v3.4.5, Kubernetes, Minikube y PowerShell.

## Restricciones globales

- No almacenar tokens, contraseñas ni kubeconfigs en Git.
- No modificar el Deployment con `kubectl set image` ni `kubectl rollout restart`.
- Verificar siempre el digest desplegado y la revisión Git observada por Argo CD.
- Generar evidencia incluso cuando falle la sincronización.

### Tarea 1: Contrato automatizado del flujo GitOps

- [x] Crear `scripts/validate-gitops.ps1` con comprobaciones de manifiestos, workflow, digest y ausencia de despliegue imperativo.
- [x] Ejecutar `powershell -File scripts/validate-gitops.ps1` y confirmar que el estado actual falla por el conflicto de `deploy-local.ps1` y por la ausencia del instalador.

### Tarea 2: Instalación reproducible de Argo CD

- [x] Crear `scripts/setup-argocd.ps1` fijado a `v3.4.5` y SHA-256 `cdf6758b489d25641c2a1fd835642543aaa64fe530867d0136a83ddf3dafe456`.
- [x] Esperar las implementaciones del namespace `argocd` y comprobar la existencia del CRD `applications.argoproj.io`.

### Tarea 3: Entrega exclusiva mediante Argo CD

- [x] Sustituir el despliegue imperativo de `scripts/deploy-local.ps1` por bootstrap, refresh, espera `Synced/Healthy` y smoke tests.
- [x] Invocar el instalador desde el job `deploy`, comprobar el digest real del Deployment y recolectar evidencia estructurada.
- [x] Mantener la promoción automática del digest en `k8s/base/kustomization.yaml`.

### Tarea 4: Documentación y verificación

- [x] Documentar instalación, sincronización, `selfHeal`, consulta del digest y descarga de evidencias.
- [x] Ejecutar validación GitOps, parseo YAML, Maven verify, render de Kustomize y comprobaciones locales de Minikube.
- [ ] Publicar la rama y confirmar el pipeline de GitHub Actions antes de integrar en `main`.
