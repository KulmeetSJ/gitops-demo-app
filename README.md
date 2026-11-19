# gitops-demo-app

[![ci](https://github.com/KulmeetSJ/gitops-demo-app/actions/workflows/ci.yml/badge.svg)](https://github.com/KulmeetSJ/gitops-demo-app/actions/workflows/ci.yml)

A small accounts REST service used to demonstrate a bank-style delivery pipeline:
commit → tested, scanned, SBOM'd image → GitOps promotion (Argo CD) → canary with automated rollback (Argo Rollouts).
The platform side lives in [gitops-demo-platform](https://github.com/KulmeetSJ/gitops-demo-platform).

> All data is synthetic. All numbers in [RESULTS.md](RESULTS.md) are personal-project benchmarks.

## The service
| Endpoint | Purpose |
|---|---|
| `POST /api/accounts` | Create an account (`ownerName`, ISO `currency`, `initialBalance`) → `201` + `Location` |
| `GET /api/accounts`, `GET /api/accounts/{id}` | Read; unknown IDs return a `404` problem detail |
| `/actuator/health/{liveness,readiness}` | Kubernetes probes |
| `/actuator/info` | Build info (artifact, version, build time) |
| `/actuator/prometheus` | Metrics, including `http_server_requests` histograms used by canary analysis |

Spring Boot 4.1 · Java 21 · PostgreSQL + Flyway · Micrometer/Prometheus.

### Fault injection (`FAULT_RATE`)
Set `FAULT_RATE` (0.0–1.0) to make that fraction of `/api/**` requests fail with HTTP 500.
Health endpoints are never affected, so a bad canary stays *Ready*. Only the metrics reveal it, which is exactly what
the Argo Rollouts analysis has to catch. Injected faults are counted in `app_injected_faults_total`.

## CI pipeline (`.github/workflows/ci.yml`)
1. **Build & test:** `mvn verify`: unit tests + Testcontainers integration tests against real PostgreSQL.
2. **Image:** multi-stage build into a digest-pinned distroless JRE, non-root UID 65532, layered jar.
3. **Trivy:** results uploaded to GitHub code scanning (SARIF); the build **fails on any fixable HIGH/CRITICAL**.
4. **SBOM:** Syft, SPDX JSON, attached as a workflow artifact.
5. **Publish:** on `main` only, pushed to GHCR tagged with the commit SHA (immutable, never `:latest`).

Dependabot keeps Maven deps, the base-image digest and the Actions current. Third-party actions are pinned by commit SHA.

## Run locally
Requires JDK 21 and Docker.
```bash
./mvnw verify                                   # all tests (starts PostgreSQL in Docker)
./mvnw spring-boot:test-run                     # app + throwaway PostgreSQL via Testcontainers, on :8080
docker build -t gitops-demo-app:local .
```
If your default JDK isn't 21: `JAVA_HOME=$(brew --prefix openjdk@21) ./mvnw verify`.

## Design decisions
See [`docs/adr/`](docs/adr/).
