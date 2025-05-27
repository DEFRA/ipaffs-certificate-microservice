#!/bin/bash

REPO_DIR="$(cd "$(dirname $0)"/.. && pwd)"
LOCAL_DEVELOPMENT_PORT=6060
SERVICE_NAME="certificate-service"
set -e

# Check prerequisites
if ! command -v helm >/dev/null 2>&1; then
  echo "\`helm\` not available in PATH. Please install Helm" >&2
  exit 1
fi
if ! command -v yq >/dev/null 2>&1; then
  echo "\`yq\` not available in PATH. Please install yq" >&2
  exit 1
fi

# Retrieve and cache secrets
[[ -s "${REPO_DIR}/deploy/dev-secrets.yaml" ]] || "${REPO_DIR}"/scripts/refresh-dev-secrets.sh

# Functions to extract the required environment variables from the normal Helm deploy to use locally.
env_config_properties() {
  env_properties "yq 'select(.kind == \"ConfigMap\").data | select(.)'"
}
env_secret_properties() {
  # Need to decode any secrets
  env_properties "yq 'select(.kind == \"Secret\").data | select(.) | map_values(@base64d)'"
}

# Expect a yq command to be passed in to get the required data from the charts
env_properties() {
  # Database port needs to be changed for services running locally
  # Blob storage needs to be changed for services running locally
  helm template --debug  "${SERVICE_NAME}" "${REPO_DIR}/deploy" \
                --set webapp.image.registry= \
                --set webapp.environment=dev \
                --values "${REPO_DIR}/deploy/dev-secrets.yaml" \
                | eval $1 \
                | sed 's/: "/=/' \
                | sed 's/"$//' \
                | sed 's/:1433;/:31433;/' \
                | sed 's/storage:10000/storage:31000/'
}

env_secret_properties > ${REPO_DIR}/.env.local.to.k8s
env_config_properties >> ${REPO_DIR}/.env.local.to.k8s

# Add the local development port to the .env.local.to.k8s file
echo "PORT=${LOCAL_DEVELOPMENT_PORT}" >> ${REPO_DIR}/.env.local.to.k8s

# Change the symbolic link for application.properties to point at .env.local.to.k8s
ln -sf ${REPO_DIR}/.env.local.to.k8s ${REPO_DIR}/service/application.properties

# Remove the service from the cluster
helm uninstall "${SERVICE_NAME}" 2>/dev/null || :

# Check if proxy to local dependencies need updating
helm dependency update "${REPO_DIR}/deploy-proxy-to-local"

# Create a proxy to local.
helm upgrade --install "${SERVICE_NAME}-proxy" "${REPO_DIR}/deploy-proxy-to-local" \
  --set proxy-to-local.application.name="${SERVICE_NAME}" \
  --set proxy-to-local.local.port=${LOCAL_DEVELOPMENT_PORT}
