# syntax=docker/dockerfile:1

# ---- build stage: compile and split the Spring Boot jar into cache-friendly layers ----
# Official Maven image (pinned by digest) so the build never downloads Maven itself:
# fewer network fetches during docker build = fewer flaky CI runs (see RESULTS.md, incident 2026-11-19).
FROM maven:3.10.0-eclipse-temurin-21-noble@sha256:f6d6c7eb62ca6a7034c17bb04a6177a8269ca38e3a1febf95094dfcd97d1305b AS build
WORKDIR /src
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline
COPY src/ src/
# Tests run in CI before the image build (Testcontainers can't run inside docker build).
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -DskipTests package \
 && cp target/gitops-demo-app-*.jar app.jar \
 && java -Djarmode=tools -jar app.jar extract --layers --destination /extracted

# ---- runtime stage: distroless JRE, no shell, no package manager, non-root ----
FROM gcr.io/distroless/java21-debian13:nonroot@sha256:6b60e44d652d66a9e2e584364c96adac4df7133674b2834b6f71741b7cdb1ff3
WORKDIR /app
# Least-frequently-changing layers first so image pulls reuse cached layers.
COPY --from=build /extracted/dependencies/ ./
COPY --from=build /extracted/spring-boot-loader/ ./
COPY --from=build /extracted/snapshot-dependencies/ ./
COPY --from=build /extracted/application/ ./
# Numeric UID so Kubernetes runAsNonRoot can verify it.
USER 65532:65532
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
