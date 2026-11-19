#!/usr/bin/env bash
# One-time setup: lets this repo's CI commit image tags to gitops-demo-platform (envs/dev), and nothing else.
#   - public key  -> deploy key on gitops-demo-platform, write access (scoped to that single repo)
#   - private key -> Actions secret PLATFORM_DEPLOY_KEY on gitops-demo-app
# The key pair only exists in a temp dir for the duration of this script.
set -euo pipefail
OWNER="${OWNER:-KulmeetSJ}"
TMP="$(mktemp -d)"; trap 'rm -rf "$TMP"' EXIT

ssh-keygen -q -t ed25519 -N "" -C "gitops-demo-app CI -> gitops-demo-platform" -f "$TMP/key"
gh repo deploy-key add "$TMP/key.pub" -R "$OWNER/gitops-demo-platform" --allow-write --title "gitops-demo-app CI (dev deploys)"
gh secret set PLATFORM_DEPLOY_KEY -R "$OWNER/gitops-demo-app" < "$TMP/key"
echo "Done. Deploy keys on gitops-demo-platform:"
gh repo deploy-key list -R "$OWNER/gitops-demo-platform"
