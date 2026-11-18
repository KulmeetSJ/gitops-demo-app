# Results

All numbers are **personal-project benchmarks**, not production results.

## Vulnerability remediation via the scan gate

| Date | Image base | Fixable HIGH | Fixable CRITICAL | Gate |
|---|---|---|---|---|
| 2026-11-18 | `gcr.io/distroless/java21-debian12:nonroot` | 9 | 1 | ❌ fail |
| 2026-11-18 | `gcr.io/distroless/java21-debian13:nonroot` (digest-pinned) | 0 | 0 | ✅ pass |

- **Method:** `trivy image --severity HIGH,CRITICAL --ignore-unfixed` on the locally built image (Trivy 0.76.2, DB updated 2026-11-18).
- **Findings:** all 10 were in base-image OS packages (`libexpat1` ×8, `libbrotli1`, `liblcms2-2`); the application's Java dependencies had none.
- **Fix:** move the runtime base from Debian 12 to Debian 13 distroless, pinned by digest, with Dependabot bumping the digest.
- **Raw output:** [`scans/trivy-baseline.json`](scans/trivy-baseline.json), [`scans/trivy-after-base-upgrade.json`](scans/trivy-after-base-upgrade.json).
- **Environment:** Apple M2, 16 GB RAM, macOS 27.0.1, Docker Desktop.

## Still to measure (Phase 4)
- Deploy lead time (commit → prod `Healthy`, approval wait reported separately)
- Rollback time (first failed analysis → stable at 100%), median of 5
- Share of deployments blocked by policy, per policy
