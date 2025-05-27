#!/bin/bash

REPO_DIR="$(cd "$(dirname $0)"/.. && pwd)"
SERVICE_NAME="certificate-service"

set -e

# Check prerequisites
if ! command -v kubectl >/dev/null 2>&1; then
  echo "\`kubectl\` not available in PATH. Please install Kubernetes command-line interface" >&2
  exit 1
fi
if ! command -v helm >/dev/null 2>&1; then
  echo "\`helm\` not available in PATH. Please install Helm" >&2
  exit 1
fi

# Remove the proxy to local if present from the cluster
helm uninstall "${SERVICE_NAME}"-proxy 2>/dev/null || :

# Retrieve and cache secrets
[[ -s "${REPO_DIR}/deploy/dev-secrets.yaml" ]] || "${REPO_DIR}"/scripts/refresh-dev-secrets.sh

# Check if dependencies need updating
helm dependency update "${REPO_DIR}/deploy"

# Install to local dev cluster using Helm
helm upgrade --install --render-subchart-notes "${SERVICE_NAME}" "${REPO_DIR}/deploy" \
  --set webapp.image.registry=localhost:30500 \
  --set webapp.image.tag=latest \
  --set webapp.environment=dev \
  --values "${REPO_DIR}/deploy/dev-secrets.yaml"

# Initiate a rollout every time to ensure new image is picked up
echo; kubectl rollout restart deployment "${SERVICE_NAME}"
