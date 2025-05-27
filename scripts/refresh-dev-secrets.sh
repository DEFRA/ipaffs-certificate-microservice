#!/bin/bash

REPO_DIR="$(cd "$(dirname $0)"/.. && pwd)"
SECRETS_FILE="${REPO_DIR}/deploy/dev-secrets.yaml"

# Check prerequisites
if ! command -v az >/dev/null 2>&1; then
  echo "\`az\` not available in PATH. Please install Azure CLI" >&2
  exit 1
fi

if [[ -z "${IPAFFS_KEYVAULT}" ]]; then
  echo "IPAFFS_KEYVAULT environment variable not set." >&2
  echo >&2
  echo "Please set this to name of the Key Vault from which to retrieve development secrets." >&2
  echo "e.g. \`export IPAFFS_KEYVAULT=fortknox\`" >&2
  exit 1
fi

set -e

retrieve_secret() {
  key_name="${1}"
  read -r -d '' value <<<"$(az keyvault secret show --vault-name "${IPAFFS_KEYVAULT}" -n "${key_name}" --query value -o tsv)"
  echo -n "${value}"
}



secrets=(
)

echo "secrets:" >"${SECRETS_FILE}"

for secret_name in "${secrets[@]}"; do
  value="$(retrieve_secret "${secret_name}")"
  escaped_value="${value//\"/\\\"}"
  echo "  ${secret_name//-/_}: \"${escaped_value}\"" >>"${SECRETS_FILE}"
done