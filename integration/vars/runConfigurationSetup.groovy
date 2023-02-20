import defra.pipeline.names.DockerName
import defra.pipeline.deploy.DeployActions
import defra.pipeline.environments.EnvironmentActions

/**
 * Run the configuration docker container
 *
 * @param serviceName        The name of the service to run configuration for
 * @param resourceGroupName  The resource group to run against
 * @param subscription       The subscription, e.g. Sandpit
 * @param version            The version
 */

def call(String serviceName, String resourceGroupName, String subscription, String version) {
    call(serviceName, resourceGroupName, subscription, "${BRANCH_NAME}", version)
}

def call(String serviceName, String resourceGroupName, String subscription, String branchName, String version) {

    try
    {
        if (fileExists('configuration/Dockerfile')) {

            String dockerNameTag = DockerName.getNameAndTag(branchName, serviceName, subscription, version, false, this)

            DeployActions.deployConfigurationOnly(resourceGroupName, serviceName, subscription, dockerNameTag, this)

         } else {
             echo "No configuration to deploy"
         }

    } catch (e) {
        echo "DEPLOY FAILED, REMOVING RESOURCE GROUP"
        EnvironmentActions env = new EnvironmentActions()
        env.destroyResourceGroup(resourceGroupName, true, subscription, true, this)
        throw e
    }
}
