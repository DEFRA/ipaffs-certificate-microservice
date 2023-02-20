package defra.pipeline.octopus

import defra.pipeline.config.Config
import defra.pipeline.deploy.DeployQueries
import defra.pipeline.names.DockerName



class OctoDocker {

    /**
     * Loop over all the microservices that should be deployed and tag each of them appropriately
     *
     * @param subscription    The subscription, e.g. Sandpit
     * @param releaseVersion  The version we are releasing from Octopus, e.g. 3.6.5
     * @param script          The global script parameter
     */
    public static void updateDockerToRelease(String subscription, String releaseVersion, Script script) {

        def componentNamesToDeploy = DeployQueries.getListOfDeployableComponents(Config.getPropertyValue("${subscription.toLowerCase()}DeploymentList", script), script)

        for (serviceName in componentNamesToDeploy) {
            if(!serviceName.contains('stub')) {
              updateDockerComponentToRelease(serviceName, subscription, releaseVersion, script)
            } else {
              script.echo "${serviceName} is a stub service not promoting to RTL"
            }
        }

    }

    /**
     * Push a docker image from SND into Octopus ACR
     *
     * @param serviceName     The microservice to tag
     * @param subscription    The subscription, e.g. Sandpit
     * @param releaseVersion  The version we are releasing from Octopus, e.g. 3.6.5
     * @param script          The global script parameter
     */
    public static void updateDockerComponentToRelease(String serviceName, String subscription, String releaseVersion, Script script) {
        def version = DeployQueries.getReleaseNumber(subscription, serviceName, script)
        def releaseType = DeployQueries.getReleaseType(subscription, serviceName, script)
        script.echo "Checking for service: ${serviceName} Version: ${version}"

        String releaseSndDockerNameTag

          if (releaseType == "hotfix") {
              releaseSndDockerNameTag = DockerName.getNameAndTag('hotfix/', serviceName, 'Sandpit', version, true, script )
          } else {
              releaseSndDockerNameTag = DockerName.getNameAndTag('master', serviceName, 'Sandpit', version, true, script )
          }

        String releaseOpsDockerNameTag = DockerName.getNameAndTag('master', serviceName, subscription, releaseVersion, true, script)
        
        script.echo "Sandpit RELEASE CONTAINER : ${releaseSndDockerNameTag}"
        script.echo "${subscription} RELEASE CONTAINER : ${releaseOpsDockerNameTag}"

        releaseNewContainer(releaseSndDockerNameTag, releaseOpsDockerNameTag, serviceName, subscription, script)
        releaseNewConfigurationContainer(releaseSndDockerNameTag, releaseOpsDockerNameTag, serviceName, subscription, script)  
    }

    /**
     * Push and tag a docker app into Octopus ACR
     *
     * @param releaseSndDockerNameTag  The tag of the container in SND release
     * @param releaseOpsDockerNameTag  The tag in Octopus that we should push to
     * @param serviceName              The microservice to tag
     * @param subscription             The subscription, e.g. Sandpit
     * @param script                   The global script parameter
     */
    public static void releaseNewContainer(String releaseSndDockerNameTag, String releaseOpsDockerNameTag, String serviceName, String subscription, Script script) {

        doDockerTagging(releaseSndDockerNameTag, releaseOpsDockerNameTag, script)

    }

    /**
     * Push and tag a docker configuration image into Octopus ACR
     *
     * @param releaseSndDockerNameTag  The tag of the container in SND release
     * @param releaseOpsDockerNameTag  The tag in Octopus that we should push to
     * @param serviceName              The microservice to tag
     * @param subscription             The subscription, e.g. Sandpit
     * @param script                   The global script parameter
     */
    public static void releaseNewConfigurationContainer(String releaseSndDockerNameTag, String releaseOpsDockerNameTag, String serviceName, String subscription, Script script) {

        if (!DeployQueries.hasConfiguration(serviceName, subscription, script)) {
            script.echo("No configuration for: ${serviceName}")
            return
        }

        script.echo "Getting Configuration Container for ${serviceName}"

        def databaseContainerName = releaseSndDockerNameTag.replace(serviceName, serviceName + "-configuration")
        def databaseReleaseContainerName = releaseOpsDockerNameTag.replace(serviceName, serviceName + "-configuration")

        doDockerTagging(databaseContainerName, databaseReleaseContainerName, script)

    }

    private static void doDockerTagging(String containerName, String releaseContainerName, Script script) {

        script.sh(script: "echo docker pull '${containerName}'")
        script.sh(script: "echo docker tag '${containerName}' '${releaseContainerName}'")
        script.sh(script: "echo docker push '${releaseContainerName}'")

    }

}
