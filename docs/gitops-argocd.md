# Operación y demostración GitOps con Argo CD

## Responsabilidades

GitHub Actions ejecuta build, pruebas, análisis DevSecOps, publicación en GHCR, firma Cosign y promoción del digest. Argo CD es el único componente que reconcilia `k8s/base` contra Minikube. El pipeline no modifica la imagen del Deployment con comandos imperativos.

## Preparación inicial

En el equipo que ejecuta el runner autoalojado:

```powershell
.\scripts\setup-minikube.ps1
.\scripts\setup-argocd.ps1
kubectl apply -f .\argocd\application.yaml
```

El instalador utiliza Argo CD `v3.4.5`, verifica el SHA-256 del manifiesto oficial y espera a que sus Deployments, StatefulSets y CRD estén disponibles.

Para abrir la interfaz sin exponerla a Internet:

```powershell
kubectl -n argocd port-forward service/argocd-server 8081:443
```

La evidencia principal puede mostrarse sin interfaz gráfica:

```powershell
kubectl -n argocd get application weather-api
kubectl -n argocd get application weather-api -o jsonpath='{.status.sync.status}{" / "}{.status.health.status}{" / "}{.status.sync.revision}{"\n"}'
kubectl -n weather-api get deployment weather-api -o jsonpath='{.spec.template.spec.containers[0].image}{"\n"}'
```

## Flujo automático

1. El job `image` publica y firma `ghcr.io/justxt/weather-api@sha256:...`.
2. `update-gitops` actualiza el campo `digest` de `k8s/base/kustomization.yaml` y crea un commit en `main`.
3. Argo CD detecta la revisión y sincroniza el clúster.
4. El job `deploy` exige simultáneamente `Synced`, `Healthy`, la revisión promovida y el digest exacto.
5. El pipeline ejecuta `/api/health` y una consulta funcional, y publica `argocd-gitops-evidence`.

## Demostración de selfHeal

Primero mostrar el estado declarado y el estado real:

```powershell
Select-String -Path .\k8s\base\deployment.yaml -Pattern 'replicas:'
kubectl -n weather-api get deployment weather-api -o jsonpath='{.spec.replicas}{"\n"}'
```

Introducir una desviación exclusivamente para la demostración:

```powershell
kubectl -n weather-api scale deployment/weather-api --replicas=1
kubectl -n argocd get application weather-api --watch
```

En otra terminal:

```powershell
kubectl -n weather-api get deployment weather-api --watch
```

Argo CD detectará `OutOfSync` y restaurará las dos réplicas declaradas. `kubectl scale` se utiliza únicamente para demostrar autorreparación; nunca forma parte del despliegue.

## Evidencias generadas

El artefacto `argocd-gitops-evidence` contiene:

- `application.yaml` y `application.json`: revisión, sincronización, salud y recursos administrados.
- `expected-state.txt`: revisión e imagen promovidas por GitHub Actions.
- `deployed-image.txt`: digest realmente configurado en el Deployment.
- `sync-status.log`: transición hasta `Synced` y `Healthy`.
- `smoke-tests.txt`: salud y respuesta funcional.
- `workloads.txt`, `argocd-pods.txt` y `events.txt`: estado operativo para diagnóstico.

Se descarga desde **GitHub > Actions > ejecución > Artifacts**.

## Ejecución manual verificable

```powershell
.\scripts\deploy-local.ps1
```

El script consulta `origin/main`, espera la misma revisión que observa Argo CD, compara el digest desplegado y ejecuta los smoke tests. No aplica `k8s/base` directamente.

## Recuperación

- Job en cola: iniciar `C:\ActionsRunner\Weather-Api\run.cmd` y comprobar que conserva las etiquetas `self-hosted`, `windows` y `weather-api-minikube`.
- Minikube apagado: iniciar Docker Desktop y ejecutar `.\scripts\setup-minikube.ps1`.
- Argo CD ausente o incompleto: ejecutar `.\scripts\setup-argocd.ps1`.
- Aplicación estancada: `kubectl -n argocd describe application weather-api` y `kubectl -n argocd get pods`.
- Rollback: revertir el commit que cambió el digest en `main`; Argo CD reconciliará el digest anterior sin `kubectl set image`.
