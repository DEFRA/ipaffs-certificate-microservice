#!/bin/bash

REPO_DIR="$(cd "$(dirname $0)"/.. && pwd)"
SERVICE_NAME="certificate-service"

set -e

# Build the service
cd "${REPO_DIR}/service"
mvn clean package -DskipTests=true

# Build container and push to local registry
docker build --platform=linux/amd64 -t "${SERVICE_NAME}":latest -f "${REPO_DIR}/service/Dockerfile.k8s" "${REPO_DIR}/service"
docker tag "${SERVICE_NAME}":latest host.docker.internal:30500/"${SERVICE_NAME}":latest
docker push host.docker.internal:30500/"${SERVICE_NAME}":latest
