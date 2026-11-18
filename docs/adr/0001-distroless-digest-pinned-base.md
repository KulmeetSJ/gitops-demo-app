# ADR 0001: Distroless, digest-pinned runtime image with a hard Trivy gate

**Status:** accepted · 2026-11-18

## Context
The image has to pass a CI gate that fails on fixable HIGH/CRITICAL vulnerabilities, and later a Kyverno
policy requiring non-root containers. The first image (distroless Debian 12 Java 21) had 10 fixable
HIGH/CRITICAL findings, all in OS packages.

## Decision
- Runtime base: `gcr.io/distroless/java21-debian13:nonroot`, **pinned by digest**.
- Run as numeric UID `65532` so Kubernetes `runAsNonRoot` can verify it without inspecting `/etc/passwd`.
- Trivy runs twice on the same loaded image: once with `exit-code 0` to publish SARIF to code scanning (visibility even
  when failing), once with `exit-code 1` as the gate. `--ignore-unfixed` keeps the gate actionable.
- Dependabot (`docker` ecosystem) proposes digest bumps weekly; each bump goes through the same gate.

## Consequences
- No shell in the image: debugging uses `kubectl debug` ephemeral containers.
- A digest pin means new CVEs don't change what we ship silently; they surface as Dependabot PRs plus scan failures.
- The gate can turn red without any code change when new CVEs are published. That is intended: it forces a rebuild
  onto a patched base.
