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

## Scan gate blocks a known-vulnerable dependency

| Date | Change | Finding | CI result |
|---|---|---|---|
| 2026-11-18 | PR [#2](https://github.com/KulmeetSJ/gitops-demo-app/pull/2) adds `org.apache.commons:commons-text:1.9` | CVE-2022-42889, CRITICAL (fixed in 1.10.0) | ❌ failed at *Trivy gate*; alert raised in code scanning; no image pushed ([run](https://github.com/KulmeetSJ/gitops-demo-app/actions/runs/44366366707)) |

- The PR was closed unmerged. Local reproduction: [`scans/trivy-vulnerable-dependency-pr.json`](scans/trivy-vulnerable-dependency-pr.json).

## CI duration

- The first green pipeline on `main` (test, then image, scan, SBOM, push) took 4m10s on GitHub-hosted `ubuntu-latest` ([run](https://github.com/KulmeetSJ/gitops-demo-app/actions/runs/44364385659)).

## Measured in Phase 4 (2026-11-19)
Methods and raw data are in [gitops-demo-platform/RESULTS.md](https://github.com/KulmeetSJ/gitops-demo-platform/blob/main/RESULTS.md).
- **CI green: median 2m40s** over 3 runs (test, image, Trivy gate, SBOM, publish).
- **Deploy lead time, commit → prod Healthy: median 17m12s** over 3 runs, excluding the human approval wait (reported separately, 1–5 min).
- **Rollback, first failed analysis → stable at 100%: 30 s** in 5/5 drills. A bad release served traffic for a median of 90 s.
- **Policy:** 7/7 non-compliant test manifests denied at admission, 0/1 compliant denied. The per-policy denial share across real deployments was not measured.
