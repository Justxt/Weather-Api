# Evidencias Argo CD

Completar este documento con capturas antes de entregar.

## Captura 1. Manifiestos en Git

Insertar captura del repo mostrando:

- `argocd/application.yaml`
- `k8s/base/*`

## Captura 2. Imagen publicada

Insertar captura del registry con la imagen `weather-api` y el tag usado.

## Captura 3. Aplicacion creada en Argo CD

Insertar captura de la pantalla principal de la aplicacion `weather-api`.

## Captura 4. Estado Healthy / Synced

Insertar captura donde se vea:

- `Healthy`
- `Synced`
- namespace `weather-api`

## Captura 5. Recursos en Kubernetes

Insertar capturas o salida de:

```bash
kubectl get pods -n weather-api
kubectl get svc -n weather-api
kubectl get deployment -n weather-api
```

## Captura 6. Prueba funcional

Insertar:

- `kubectl port-forward svc/weather-api -n weather-api 8080:80`
- POST exitoso a `http://localhost:8080/api/flight-cancellation-risk`

## Captura 7. Cambio en la fuente de verdad

Modificar [k8s/base/deployment.yaml](/C:/Code/Weather-Api/k8s/base/deployment.yaml), por ejemplo:

```yaml
replicas: 2 -> replicas: 3
```

Insertar captura del commit o diff.

## Captura 8. OutOfSync en Argo CD

Insertar captura del estado antes de sincronizar.

## Captura 9. Sincronizacion aplicada

Insertar captura de Argo CD despues de sincronizar y de:

```bash
kubectl get pods -n weather-api
```
