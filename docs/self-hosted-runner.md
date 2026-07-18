# Runner autoalojado para Minikube

El job de despliegue se ejecuta en una maquina Windows controlada por el equipo. GitHub Actions coordina el pipeline en la nube; el runner actua como puente hacia el cluster Minikube local, que no es accesible desde los runners hospedados por GitHub.

## Requisitos

- Windows 11 con Docker Desktop iniciado.
- Minikube y kubectl disponibles en `PATH`. El workflow descarga Cosign y el manifiesto oficial de Argo CD con versiones y SHA-256 fijados.
- Perfil `minikube` creado con `scripts/setup-minikube.ps1`.
- GitHub Actions Runner instalado bajo una cuenta sin privilegios administrativos permanentes.
- Salida HTTPS hacia GitHub, GHCR, Sigstore y Open-Meteo.

## Registro

1. Abrir **Settings > Actions > Runners > New self-hosted runner** en el repositorio.
2. Elegir Windows x64 y ejecutar los comandos mostrados por GitHub en una carpeta dedicada fuera del repositorio.
3. Durante `config.cmd`, registrar las etiquetas `windows` y `weather-api-minikube`.
4. Utilizar el token temporal mostrado por GitHub solo durante el registro. No copiarlo a archivos, scripts ni variables versionadas.
5. Ejecutar `run.cmd` con la misma cuenta de Windows que posee el perfil Minikube. Para la demostracion academica no es necesario instalarlo como servicio.

El workflow selecciona el host con:

```yaml
runs-on: [self-hosted, windows, weather-api-minikube]
```

## Configuracion de GitHub

- Crear el environment `minikube` en **Settings > Environments**.
- Mantener `main` protegida y exigir la aprobacion del pull request y los checks del workflow.
- La autenticacion a GHCR usa `GITHUB_TOKEN`; no se necesita un PAT almacenado.

## Operacion y recuperacion

Comprobar el host antes de una entrega:

```powershell
docker version
minikube status --profile minikube
kubectl config use-context minikube
kubectl get nodes
kubectl -n argocd get pods
kubectl -n argocd get application weather-api
cosign version
```

Si el job permanece en espera, confirmar que `C:\ActionsRunner\Weather-Api\run.cmd` esté activo y que la etiqueta coincida. Si Minikube perdió su contexto, ejecutar `scripts/setup-minikube.ps1`; si Argo CD no está disponible, ejecutar `scripts/setup-argocd.ps1`.

El rollback también debe respetar GitOps: se revierte en `main` el commit que cambió el digest y Argo CD restaura el estado declarado. No se utiliza `kubectl rollout undo` ni `kubectl set image`, porque ambos crearían una desviación que `selfHeal` corregiría.

Para consultar el estado:

```powershell
kubectl -n argocd get application weather-api
kubectl -n weather-api get deployment weather-api -o jsonpath='{.spec.template.spec.containers[0].image}{"\n"}'
```

El pipeline verifica la firma Cosign antes de promover el digest. Un fallo de firma, sincronización, salud, digest, probes o smoke test detiene el despliegue y conserva evidencia en GitHub Actions.
