FROM maven:3.9.9-eclipse-temurin-21@sha256:3a4ab3276a087bf276f79cae96b1af04f53731bec53fb2e651aca79e4b10211e AS build

WORKDIR /workspace

COPY pom.xml mvnw ./
COPY .mvn .mvn

RUN chmod +x mvnw && ./mvnw -B -ntp dependency:go-offline

COPY src src
COPY config config
COPY docker docker

RUN ./mvnw -B -ntp clean package -DskipTests \
    && javac --release 21 -d /workspace/healthcheck /workspace/docker/HealthCheck.java

FROM gcr.io/distroless/java21-debian13:nonroot@sha256:c1ab839be0b871268e437a008e154be87f8fabca0202dcd393633c7b263b8e78

WORKDIR /app

COPY --from=build --chown=10001:10001 /workspace/target/ApiWeather-0.0.1-SNAPSHOT.jar app.jar
COPY --from=build --chown=10001:10001 /workspace/healthcheck /app/healthcheck

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -Djava.io.tmpdir=/tmp"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=20s --retries=3 \
  CMD ["/usr/bin/java", "-cp", "/app/healthcheck", "HealthCheck"]

USER 10001:10001

ENTRYPOINT ["/usr/bin/java", "-jar", "/app/app.jar"]
