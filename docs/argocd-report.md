# Informe de Despliegue con Argo CD

## Integrantes

- Nombre: ______________________
- Curso: ______________________
- Fecha: ______________________

## Tema

Despliegue de una aplicacion funcional en Kubernetes usando Argo CD, aplicando el enfoque GitOps.

## Objetivo

El objetivo de esta practica fue desplegar el proyecto `Weather-Api` en un cluster de Kubernetes usando Argo CD, de forma que el repositorio Git funcione como fuente de verdad y los cambios hechos en los manifiestos se reflejen en el cluster mediante sincronizacion.

## Descripcion del proyecto

La aplicacion utilizada fue `Weather-Api`, desarrollada en Spring Boot. Esta API permite consultar el riesgo de cancelacion de un vuelo segun condiciones climaticas, recibiendo como entrada una ciudad y una fecha.

La aplicacion expone principalmente el endpoint:

```http
POST /api/flight-cancellation-risk
```

Ademas, para poder validar el despliegue en Kubernetes, se agrego un endpoint de salud:

```http
GET /api/health
```

Este endpoint se utilizo para las probes de Kubernetes.

## Herramientas utilizadas

- GitHub como repositorio y fuente de verdad
- Docker para construir la imagen
- Kubernetes para ejecutar la aplicacion
- Argo CD para la sincronizacion GitOps
- Spring Boot como framework de la aplicacion

## Archivos implementados

Para realizar el despliegue se agregaron los siguientes archivos:

- `Dockerfile`
- `.dockerignore`
- `k8s/base/namespace.yaml`
- `k8s/base/deployment.yaml`
- `k8s/base/service.yaml`
- `k8s/base/kustomization.yaml`
- `argocd/application.yaml`

Tambien se agregaron documentos de apoyo:

- `docs/argocd-implementation.md`
- `docs/argocd-evidence-template.md`

## Explicacion de la implementacion

### 1. Dockerizacion de la aplicacion

Primero se creo un `Dockerfile` para empaquetar la aplicacion Spring Boot en una imagen Docker. La construccion se realiza en dos etapas:

1. Se compila el proyecto con Maven.
2. Se copia el `.jar` generado a una imagen mas liviana con Java 21.

Con esto la aplicacion queda lista para ser ejecutada dentro del cluster.

### 2. Manifiestos de Kubernetes

Se creo un conjunto de manifiestos en la carpeta `k8s/base`.

#### Namespace

Se definio el namespace `weather-api` para aislar los recursos de esta aplicacion.

#### Deployment

Se definio un `Deployment` con:

- nombre `weather-api`
- 2 replicas
- contenedor exponiendo el puerto `8080`
- `readinessProbe` y `livenessProbe` apuntando a `/api/health`

Esto permite que Kubernetes mantenga la aplicacion disponible y reinicie el contenedor si falla.

#### Service

Se definio un `Service` tipo `ClusterIP` para exponer la aplicacion dentro del cluster y permitir acceso por red al deployment.

#### Kustomization

Se utilizo `kustomization.yaml` para centralizar la referencia de la imagen y el tag, de manera que sea mas facil cambiar la version desplegada.

## Configuracion de Argo CD

Se creo el manifiesto `argocd/application.yaml` para registrar la aplicacion en Argo CD.

En este archivo se definio:

- el repositorio GitHub del proyecto
- la rama `main`
- el path `k8s/base`
- el namespace destino `weather-api`
- sincronizacion automatica con `prune` y `selfHeal`

Esto hace que Argo CD observe el repositorio y compare constantemente el estado deseado en Git con el estado real del cluster.

## Proceso realizado

El proceso seguido fue el siguiente:

1. Se construyo la imagen Docker de la aplicacion.
2. Se publico la imagen en un registry.
3. Se actualizo `k8s/base/kustomization.yaml` con la imagen real.
4. Se aplico `argocd/application.yaml` en Argo CD.
5. Argo CD sincronizo los manifiestos con el cluster.
6. Se verifico que el estado aparezca como `Healthy` y `Synced`.
7. Se realizo una prueba funcional del endpoint.

## Evidencia de sincronizacion GitOps

Para demostrar que Git es la fuente de verdad, se realizo un cambio en el manifiesto del deployment.

Ejemplo:

```yaml
replicas: 2
```

se cambio por:

```yaml
replicas: 3
```

Despues de hacer commit y push al repositorio:

- Argo CD detecto que la aplicacion estaba `OutOfSync`
- luego sincronizo el cambio
- finalmente el estado volvio a `Synced`
- en Kubernetes se pudo observar el aumento del numero de pods

Con esto se demuestra que los cambios hechos en Git se reflejan en el cluster.

## Resultados obtenidos

Los resultados de la practica fueron los siguientes:

- la aplicacion se pudo preparar para despliegue en Kubernetes
- se definieron manifiestos claros para namespace, deployment y service
- Argo CD pudo usar el repositorio como fuente de verdad
- se evidencio el mecanismo de sincronizacion GitOps
- se dejo documentado el procedimiento y la evidencia requerida

## Dificultades encontradas

Durante la practica fue importante considerar algunos puntos:

- la imagen Docker debe existir en un registry accesible desde el cluster
- el nombre de la imagen debe coincidir con el definido en `kustomization.yaml`
- Argo CD necesita que el path del repositorio este bien configurado
- las probes de Kubernetes requieren un endpoint de salud funcional

## Conclusion

En esta practica se logro preparar el despliegue de una aplicacion funcional usando Argo CD y Kubernetes bajo el enfoque GitOps. La parte mas importante fue comprobar que el repositorio Git actua como fuente de verdad, ya que cualquier cambio realizado en los manifiestos puede ser detectado y sincronizado por Argo CD hacia el cluster.

Tambien se entendio mejor la relacion entre Docker, Kubernetes y Argo CD dentro del proceso de entrega continua.

## Anexos

Agregar capturas de:

1. Repositorio con los manifiestos
2. Imagen publicada en el registry
3. Aplicacion creada en Argo CD
4. Estado `Healthy` y `Synced`
5. `kubectl get pods -n weather-api`
6. `kubectl get svc -n weather-api`
7. Prueba funcional del endpoint
8. Cambio en Git
9. Estado `OutOfSync`
10. Estado final sincronizado
