import defra.pipeline.config.Config
import defra.pipeline.names.DockerName

/**
 * Build the configuration docker image for setting up the service
 *
 * @param serviceName   The name of the service to run against
 * @param subscription  The subscription, e.g. Sandpit
 * @param branchName    The branch name
 * @param version       The version number
 */

def call(String serviceName, String subscription, String version) {

    call(serviceName, subscription, "${BRANCH_NAME}", version)

}

def call(String serviceName, String subscription, String branchName, String version) {

    if (fileExists('configuration/Dockerfile')) {
        baseImage = Config.getPropertyValue("javaBaseImage", this)
        linuxBaseImage = Config.getPropertyValue("linuxBaseImage", this)

        echo "Building configuration container"
        def dockerNameTag = DockerName.getConfigurationNameAndTag(branchName, serviceName, subscription, version, false, this)
        sh(script: "docker build --build-arg BASE_IMAGE=${baseImage} --build-arg LINUX_BASE_IMAGE=${linuxBaseImage} -t ${dockerNameTag} -f configuration/Dockerfile configuration/.")
    } else {
        echo "No configuration dockerfile, configuration container will be skipped"
    }

}
