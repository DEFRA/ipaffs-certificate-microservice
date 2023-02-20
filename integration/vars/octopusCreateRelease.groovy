def call(String octopusProject, String octopusApiKey, String releaseName, String channel) {
     sh(script: "docker run --rm octopusdeploy/octo:7.4.3424 create-release --apiKey ${octopusApiKey} --project ${octopusProject} --server https://octopus-ops.azure.defra.cloud --version ${releaseName} --channel ${channel} --outputformat=json")
}
