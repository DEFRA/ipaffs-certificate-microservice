def call(String buildNumber, String token, String serviceName) {
  echo "Tagging Branch: ${buildNumber}"
  sh "git tag ${buildNumber}"
  sh "git push https://jenkins:${token}@giteux.azure.defra.cloud/imports/${serviceName}.git ${buildNumber}"
}