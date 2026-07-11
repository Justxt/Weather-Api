# Informe de integraciones DevOps del proyecto Weather-Api

## Objetivo

Este informe resume las herramientas DevOps encontradas en el repositorio, el nivel de integracion de cada una y la evidencia tecnica que permite comprobarlo. El objetivo es identificar que herramientas estan bien integradas, cuales estan parcialmente integradas y que mejoras faltan para llegar a una cadena CI/CD completa.

## Resumen ejecutivo

El proyecto cuenta con una base DevOps amplia: CI con GitHub Actions, Jenkins y CircleCI; analisis de calidad con SonarCloud y JaCoCo; analisis DAST con OWASP ZAP; construccion de imagen con Docker; manifiestos Kubernetes; y declaracion de despliegue GitOps con Argo CD.

La integracion mas fuerte actualmente es la de GitHub Actions con Maven, tests, Checkstyle, JaCoCo y SonarCloud. Kubernetes y Argo CD estan presentes y bien estructurados a nivel declarativo, pero falta conectarlos con una imagen publicada en un registro y con un flujo automatico que actualice la version desplegada.

## Matriz de estado

| Herramienta | Estado | Evidencia en el repo | Que hace actualmente | Que falta |
| --- | --- | --- | --- | --- |
| GitHub Actions | Bien integrada | `.github/workflows/build.yml` | Ejecuta Maven `verify`, SonarCloud y quality gate | Depende del secreto `SONAR_TOKEN` |
| SonarCloud | Bien integrada | `.github/workflows/build.yml`, `pom.xml` | Analisis estatico, quality gate y cobertura JaCoCo | Verificar en dashboard que el quality gate quede aprobado |
| JaCoCo | Bien integrada | `pom.xml` | Genera reporte XML de cobertura para SonarCloud | Mantener cobertura sobre nuevo codigo |
| Checkstyle | Bien integrada | `pom.xml`, `config/checkstyle/checkstyle.xml` | Valida estilo de codigo en Maven | Reglas actuales son basicas |
| OWASP ZAP | Bien integrada | `.github/workflows/build.yml` | Ejecuta DAST baseline contra la API levantada en CI y guarda reportes | Ajustar reglas para decidir que alertas deben fallar el pipeline |
| Jenkins | Parcialmente integrada | `Jenkinsfile`, `jenkins/WeatherPipeline.groovy` | Compila, prueba, valida calidad y empaqueta JAR | No publica imagen Docker ni despliega |
| CircleCI | Parcialmente integrada | `.circleci/config.yml` | Compila, lint, tests paralelos, artefactos y notificaciones opcionales | No ejecuta Sonar ni despliegue |
| Docker | Parcialmente integrada | `Dockerfile` | Construye imagen multi-stage de la API | No hay pipeline que haga push a registry |
| Kubernetes | Parcialmente integrada | `k8s/base/*.yaml` | Define namespace, deployment, service, probes y recursos | Imagen `weather-api:1.0.0` no apunta a registry real |
| Argo CD | Parcialmente integrada | `argocd/application.yaml` | Define aplicacion GitOps hacia `k8s/base` con sync automatico | Falta confirmar cluster y sincronizacion real |

## Evidencia tecnica

### GitHub Actions

Archivo: `.github/workflows/build.yml`

Evidencia:

```yaml
on:
  push:
    branches:
      - main
  pull_request:
    types: [opened, synchronize, reopened]
```

El workflow se ejecuta en `push` a `main` y en pull requests. Tambien configura Java 21 y cache de Maven.

Comando principal:

```bash
./mvnw -B verify \
  org.sonarsource.scanner.maven:sonar-maven-plugin:5.7.0.6970:sonar \
  -Dsonar.projectKey=SebasDh56_Weather-Api \
  -Dsonar.organization=sebasdh56 \
  -Dsonar.qualitygate.wait=true
```

Conclusion: esta es la integracion CI mas completa porque compila, prueba, genera cobertura y espera el resultado del quality gate.

### SonarCloud y JaCoCo

Archivos: `.github/workflows/build.yml`, `pom.xml`

Evidencia en `pom.xml`:

```xml
<sonar.coverage.jacoco.xmlReportPaths>
    ${project.build.directory}/site/jacoco/jacoco.xml
</sonar.coverage.jacoco.xmlReportPaths>
```

El proyecto usa JaCoCo para generar el reporte de cobertura que SonarCloud necesita. La verificacion local con Maven paso correctamente con:

```powershell
$env:MAVEN_OPTS='-Djavax.net.ssl.trustStoreType=Windows-ROOT'; .\mvnw.cmd -B verify
```

Resultado local:

- Checkstyle: 0 violaciones.
- Tests: 21 ejecutados, 0 fallos.
- JaCoCo: reporte generado en `target/site/jacoco/jacoco.xml`.

Conclusion: SonarCloud esta bien integrado a nivel de configuracion. La condicion externa es tener configurado el secreto `SONAR_TOKEN` en GitHub.

### OWASP ZAP DAST

Archivo: `.github/workflows/build.yml`

El workflow levanta la API localmente despues del build, espera el endpoint `/api/health` y ejecuta OWASP ZAP Baseline desde Docker contra `http://host.docker.internal:8080/api/health`.

Reportes generados:

- `zap-baseline-report.html`
- `zap-baseline-report.md`
- `zap-baseline-report.json`

Conclusion: OWASP ZAP esta integrado como herramienta DAST. Esta configurado para generar evidencia sin bloquear el pipeline por advertencias iniciales, usando `-I`. El siguiente paso seria crear una politica de reglas para convertir hallazgos importantes en fallos del pipeline.

### Jenkins

Archivos: `Jenkinsfile`, `jenkins/WeatherPipeline.groovy`

Etapas detectadas:

- Checkout.
- Compilacion.
- Validacion en paralelo:
  - Tests.
  - Calidad de codigo.
- Build para ramas protegidas.

Evidencia:

```groovy
stage('Validacion') {
    parallel {
        stage('Tests') { ... }
        stage('Calidad de codigo') { ... }
    }
}
```

Conclusion: Jenkins esta integrado como pipeline CI. Es parcial porque no publica imagen Docker, no actualiza Kubernetes y no despliega.

### CircleCI

Archivo: `.circleci/config.yml`

Jobs detectados:

- `prepare`
- `lint`
- `test_parallel`
- `package`

Evidencia:

```yaml
workflows:
  weather-api-pipeline:
    jobs:
      - prepare
      - lint
      - test_parallel
      - package
```

Conclusion: CircleCI esta bien definido para CI, incluso con tests paralelos y artefactos. Es parcial porque no ejecuta SonarCloud ni despliegue.

### Docker

Archivo: `Dockerfile`

Evidencia:

```dockerfile
FROM maven:3.9.9-eclipse-temurin-21 AS build
...
FROM eclipse-temurin:21-jre
...
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

Conclusion: Docker esta integrado para construir una imagen productiva con multi-stage build. Falta un pipeline que haga `docker build`, `docker push` y publique la imagen en Docker Hub, GHCR, ECR u otro registry.

### Kubernetes

Archivos: `k8s/base/deployment.yaml`, `service.yaml`, `namespace.yaml`, `kustomization.yaml`

Evidencia:

- Deployment con 4 replicas.
- Probes de salud en `/api/health`.
- Requests y limits de CPU/memoria.
- Service `ClusterIP`.
- Kustomize para versionar imagen.

Conclusion: Kubernetes esta presente con una configuracion base correcta. Es parcial porque la imagen configurada es `weather-api:1.0.0`, sin registry externo. En un cluster real, Argo CD necesita una imagen accesible desde el cluster.

### Argo CD

Archivo: `argocd/application.yaml`

Evidencia:

```yaml
source:
  repoURL: https://github.com/Justxt/Weather-Api.git
  targetRevision: main
  path: k8s/base
syncPolicy:
  automated:
    prune: true
    selfHeal: true
```

Conclusion: Argo CD esta declarado correctamente para GitOps y sincronizacion automatica. Es parcial hasta comprobar que la aplicacion existe en un cluster Argo CD real y que la imagen de Kubernetes puede descargarse desde un registry.

## Diagnostico general

El proyecto tiene una buena base de automatizacion. La calidad de codigo y la validacion por CI estan bien cubiertas. La parte de despliegue esta disenada, pero todavia no se ve completamente cerrada porque falta el enlace entre CI, registry de imagenes, Kubernetes y Argo CD.

## Recomendaciones

1. Mantener GitHub Actions como pipeline principal de calidad porque ya integra Maven, tests, JaCoCo, SonarCloud y OWASP ZAP.
2. Definir una politica de severidad para OWASP ZAP, separando alertas informativas, warnings aceptados y fallos bloqueantes.
3. Agregar una etapa de Docker build y push a un registry.
4. Cambiar `k8s/base/kustomization.yaml` para usar una imagen real, por ejemplo `ghcr.io/usuario/weather-api:1.0.0`.
5. Automatizar la actualizacion del tag de imagen en Kubernetes despues de cada release.
6. Verificar Argo CD en el cluster con `argocd app get weather-api` y `argocd app sync weather-api`.
7. Decidir si Jenkins y CircleCI son pipelines alternativos o si uno debe quedar como principal para evitar duplicidad.

## Conclusion para exposicion

El repositorio no solo contiene codigo de la API; tambien incluye una arquitectura DevOps completa en configuracion. La parte mas madura es CI y calidad con GitHub Actions, Maven, Checkstyle, JaCoCo, SonarCloud y OWASP ZAP. La parte de CD existe como base mediante Docker, Kubernetes y Argo CD, pero requiere completar la publicacion de imagenes y validar la sincronizacion real en un cluster.
