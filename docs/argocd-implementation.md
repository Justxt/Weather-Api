# Despliegue con Argo CD

## Objetivo

Desplegar `Weather-Api` en Kubernetes usando Argo CD con un flujo GitOps donde Git actua como fuente de verdad.

## Archivos entregados

- [Dockerfile](/C:/Code/Weather-Api/Dockerfile)
- [argocd/application.yaml](/C:/Code/Weather-Api/argocd/application.yaml)
- [k8s/base/kustomization.yaml](/C:/Code/Weather-Api/k8s/base/kustomization.yaml)
- [k8s/base/namespace.yaml](/C:/Code/Weather-Api/k8s/base/namespace.yaml)
- [k8s/base/deployment.yaml](/C:/Code/Weather-Api/k8s/base/deployment.yaml)
- [k8s/base/service.yaml](/C:/Code/Weather-Api/k8s/base/service.yaml)

## Arquitectura

1. Se construye una imagen Docker de la API Spring Boot.
2. La imagen se publica en un registro accesible por el cluster.
3. Argo CD observa este repositorio y sincroniza `k8s/base`.
4. Kubernetes despliega la aplicacion en el namespace `weather-api`.

## Preparacion

### 1. Construir y publicar la imagen

Actualizar la imagen en [k8s/base/kustomization.yaml](/C:/Code/Weather-Api/k8s/base/kustomization.yaml):

```yaml
images:
  - name: weather-api
    newName: docker.io/TU_USUARIO/weather-api
    newTag: "1.0.0"
```

Construir y publicar:

```bash
docker build -t docker.io/TU_USUARIO/weather-api:1.0.0 .
docker push docker.io/TU_USUARIO/weather-api:1.0.0
```

### 2. Crear la aplicacion en Argo CD

Aplicar:

```bash
kubectl apply -n argocd -f argocd/application.yaml
```

### 3. Sincronizar

Desde Argo CD:

- abrir la aplicacion `weather-api`
- verificar estado `Healthy` y `Synced`

O por CLI:

```bash
argocd app sync weather-api
argocd app get weather-api
```

## Evidencia requerida

Tomar capturas de:

1. Repositorio con los manifiestos `k8s` y `argocd`.
2. Imagen publicada en Docker Hub u otro registry.
3. Vista de Argo CD mostrando la aplicacion `weather-api`.
4. Estado `Healthy` y `Synced`.
5. Recursos creados en el cluster:
   - `kubectl get pods -n weather-api`
   - `kubectl get svc -n weather-api`
   - `kubectl get deployment -n weather-api`
6. Prueba funcional de la API:
   - `kubectl port-forward svc/weather-api -n weather-api 8080:80`
   - llamada POST a `/api/flight-cancellation-risk`
7. Evidencia de sincronizacion GitOps:
   - cambiar `replicas: 2` a `replicas: 3` en [k8s/base/deployment.yaml](/C:/Code/Weather-Api/k8s/base/deployment.yaml)
   - hacer commit y push
   - mostrar en Argo CD el estado `OutOfSync`
   - ejecutar `Sync` o esperar autosync
   - mostrar luego `Synced` y `kubectl get pods -n weather-api`

## Guion tecnico para la exposicion

1. El repositorio Git es la fuente de verdad.
2. Argo CD observa el path `k8s/base`.
3. Cuando el manifiesto cambia en Git, Argo CD detecta drift.
4. La sincronizacion aplica el cambio en Kubernetes.
5. El ejemplo mas simple de evidencia es cambiar replicas o la tag de la imagen.

## Riesgos y supuestos

- El cluster debe poder descargar la imagen del registry elegido.
- La API depende de servicios externos de clima y geocodificacion con salida a internet.
- Antes de aplicar en Argo CD se debe reemplazar `CHANGE_ME` por el usuario real del registry.
