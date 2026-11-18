# syntax=docker/dockerfile:1

# ---- build stage: compile and split the Spring Boot jar into cache-friendly layers ----
FROM eclipse-temurin:21-jdk-noble AS build
WORKDIR /src
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -q dependency:go-offline
COPY src/ src/
# Tests run in CI before the image build (Testcontainers can't run inside docker build).
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -q -DskipTests package \
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
