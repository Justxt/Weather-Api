# CircleCI para Weather-Api

## Estructura del pipeline

La configuración vive en [`.circleci/config.yml`](/C:/Code/Weather-Api/.circleci/config.yml) y define un workflow con estos jobs:

- `prepare`: descarga dependencias y valida que el proyecto compile con `./mvnw -B -ntp clean compile`.
- `lint`: ejecuta `checkstyle`.
- `test_parallel`: corre las pruebas en paralelo con `parallelism: 3`.
- `package`: genera el `.jar` únicamente en ramas protegidas (`main` y `develop`).

## Paralelismo en CircleCI

CircleCI crea varios ejecutores idénticos cuando un job usa `parallelism` mayor a `1`. En este proyecto:

- se buscan las clases `src/test/java/**/*Tests.java`
- se convierten a nombres de clase Java
- se reparten entre nodos con `circleci tests split --split-by=timings --timings-type=classname`
- cada nodo ejecuta solo su subconjunto con Maven

Esto permite que el reparto mejore con cada corrida porque CircleCI usa tiempos históricos para equilibrar mejor las pruebas.

## Notificaciones post

Se agregaron pasos post con `when: on_success` y `when: on_fail` para notificar el estado de los jobs al finalizar.

Variable requerida en CircleCI:

- `PIPELINE_NOTIFICATION_WEBHOOK`

El webhook recibe un JSON como este:

```json
{
  "status": "success | failed",
  "project": "ApiWeather",
  "branch": "main",
  "job": "test_parallel",
  "build_num": "123",
  "build_url": "https://circleci.com/...",
  "workflow_id": "uuid",
  "commit": "sha",
  "color": "good | danger"
}
```

Puedes apuntarlo a Slack, Teams, Discord o a un servicio interno que transforme el mensaje al formato que necesites.
