def call(String resourceGroup, String serviceName) {
  sh(script: "az webapp log download --name ${serviceName} --resource-group ${resourceGroup}")
  archiveArtifacts '*.zip'
}
