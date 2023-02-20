def call(String octopusProject, String octopusApiKey, String targetEnv, String dockerTag, String channel) {
     sh(script: "docker run --rm octopusdeploy/octo:7.4.3424 deploy-release --apiKey ${octopusApiKey} --project ${octopusProject} --server https://octopus-ops.azure.defra.cloud --deployto=${targetEnv} --variable=dockerTag=${dockerTag} --channel ${channel} --version latest --progress --waitfordeployment --deploymenttimeout=06:00:00")
}
