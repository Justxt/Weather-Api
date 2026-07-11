# Presentacion: Estado de herramientas DevOps en Weather-Api

## Diapositiva 1: Titulo

**Estado de integracion DevOps del proyecto Weather-Api**

Guion:

> En esta exposicion voy a mostrar que herramientas DevOps tiene el proyecto, como se comprueba su existencia en el repositorio y que tan completa esta su integracion.

## Diapositiva 2: Herramientas encontradas

**Herramientas presentes**

- GitHub Actions
- Jenkins
- CircleCI
- SonarCloud
- OWASP ZAP
- JaCoCo
- Checkstyle
- Docker
- Kubernetes
- Argo CD

Guion:

> El proyecto tiene herramientas para integracion continua, calidad de codigo, contenedores y despliegue declarativo. No todas estan al mismo nivel de madurez.

## Diapositiva 3: CI principal

**GitHub Actions**

Evidencia:

- Archivo: `.github/workflows/build.yml`
- Ejecuta en `push` a `main` y en pull requests.
- Usa Java 21.
- Ejecuta Maven `verify`.
- Ejecuta SonarCloud.
- Espera el quality gate.

Guion:

> GitHub Actions es la integracion mas fuerte porque valida el codigo automaticamente y bloquea si SonarCloud no aprueba el quality gate.

## Diapositiva 4: Calidad de codigo

**SonarCloud + JaCoCo + Checkstyle**

Evidencia:

- SonarCloud esta en `build.yml`.
- JaCoCo esta en `pom.xml`.
- Checkstyle esta en `pom.xml` y `config/checkstyle/checkstyle.xml`.

Resultado local:

- 21 tests ejecutados.
- 0 fallos.
- 0 violaciones de Checkstyle.
- Reporte JaCoCo generado.

Guion:

> La calidad esta bien integrada porque se revisa estilo, pruebas, cobertura y quality gate. La condicion importante es que GitHub tenga configurado el secreto `SONAR_TOKEN`.

## Diapositiva 5: Jenkins

**DAST con OWASP ZAP**

Evidencia:

- Esta en `.github/workflows/build.yml`.
- Levanta la API en GitHub Actions.
- Espera `/api/health`.
- Ejecuta `zap-baseline.py` contra la API.
- Guarda reporte HTML, Markdown y JSON.

Guion:

> Para seguridad dinamica se integro OWASP ZAP Baseline. Esta herramienta prueba la aplicacion ya ejecutandose, por eso se considera DAST. Es una integracion sencilla porque no exige modificar la logica del codigo.

## Diapositiva 6: Jenkins

**Pipeline Jenkins**

Evidencia:

- Archivo: `Jenkinsfile`
- Helper: `jenkins/WeatherPipeline.groovy`

Etapas:

- Checkout.
- Compilacion.
- Tests.
- Calidad de codigo.
- Empaquetado en ramas protegidas.

Guion:

> Jenkins funciona como alternativa de CI. Esta bien para compilar y validar, pero no llega a despliegue automatico.

## Diapositiva 7: CircleCI

**Pipeline CircleCI**

Evidencia:

- Archivo: `.circleci/config.yml`

Jobs:

- `prepare`
- `lint`
- `test_parallel`
- `package`

Guion:

> CircleCI tambien esta preparado como CI. Tiene una ventaja: ejecuta tests en paralelo y guarda artefactos. Pero no ejecuta SonarCloud ni despliega.

## Diapositiva 8: Docker

**Contenerizacion**

Evidencia:

- Archivo: `Dockerfile`
- Usa multi-stage build.
- Construye el JAR con Maven.
- Ejecuta la app con Java 21 JRE.

Guion:

> Docker esta integrado para empaquetar la API en una imagen. La parte pendiente es publicar esa imagen en un registry para que Kubernetes pueda consumirla.

## Diapositiva 9: Kubernetes

**Manifiestos Kubernetes**

Evidencia:

- `k8s/base/deployment.yaml`
- `k8s/base/service.yaml`
- `k8s/base/namespace.yaml`
- `k8s/base/kustomization.yaml`

Incluye:

- 4 replicas.
- Health checks en `/api/health`.
- Requests y limits.
- Service interno.

Guion:

> Kubernetes esta configurado a nivel base. Tiene buenas practicas como probes y limites de recursos. Lo pendiente es usar una imagen real publicada en un registry.

## Diapositiva 10: Argo CD

**GitOps con Argo CD**

Evidencia:

- Archivo: `argocd/application.yaml`
- Apunta a `k8s/base`.
- Usa `targetRevision: main`.
- Tiene sync automatico con `prune` y `selfHeal`.

Guion:

> Argo CD esta declarado para hacer despliegue GitOps. Esto significa que el cluster puede sincronizarse desde el repositorio. Falta comprobarlo en un cluster real.

## Diapositiva 11: Matriz final

| Herramienta | Estado |
| --- | --- |
| GitHub Actions | Bien integrada |
| SonarCloud | Bien integrada |
| OWASP ZAP | Bien integrada |
| JaCoCo | Bien integrada |
| Checkstyle | Bien integrada |
| Jenkins | Parcial |
| CircleCI | Parcial |
| Docker | Parcial |
| Kubernetes | Parcial |
| Argo CD | Parcial |

Guion:

> La conclusion es que CI, calidad y seguridad DAST estan fuertes. La parte de despliegue existe, pero falta cerrar el ciclo completo: construir imagen, publicarla, actualizar Kubernetes y sincronizar con Argo CD.

## Diapositiva 12: Que falta para CI/CD completo

**Pendientes principales**

- Publicar imagen Docker en un registry.
- Definir que alertas de OWASP ZAP deben fallar el pipeline.
- Usar esa imagen real en `kustomization.yaml`.
- Automatizar el cambio de tag.
- Validar Argo CD en un cluster.
- Definir si se usara GitHub Actions, Jenkins o CircleCI como pipeline principal.

Guion:

> El siguiente paso tecnico seria unir CI con CD. Es decir, que despues de aprobar tests y Sonar, el pipeline publique la imagen y Argo CD despliegue esa version.

## Diapositiva 13: Cierre

**Conclusion**

El proyecto tiene una arquitectura DevOps avanzada para una API pequena. La calidad, validacion automatica y seguridad DAST estan bien integradas. El despliegue esta disenado con Docker, Kubernetes y Argo CD, pero aun necesita conexion real con un registry y un cluster.

Guion:

> En resumen, el proyecto ya tiene la base profesional de CI/CD con analisis de calidad y seguridad dinamica. Lo mas fuerte es la validacion automatica. Lo que falta es completar el despliegue continuo real.
