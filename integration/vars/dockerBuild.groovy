/**
 * Build the main service docker image
 *
 * @param serviceName   The service name to build the docker image for
 * @param subscription  The subscription we're building for, e.g. Sandpit
 * @param version       The version number
 * @param serviceType   The type of service being built e.g. Java or Node
 */

import defra.pipeline.names.DockerName
import defra.pipeline.config.Config

def call(String serviceName, String subscription, String version, String serviceType) {

    return call(serviceName, "${BRANCH_NAME}", false, subscription, version, serviceType)
}

def call(String serviceName, String branchName, boolean isMasterRelease, String subscription, String version, String serviceType) {

    def dockerNameTag = DockerName.getNameAndTag(branchName, serviceName, subscription, version, isMasterRelease, this)

    if (serviceType == 'Java') {
        baseImage = Config.getPropertyValue("javaBaseImage", this)
        applicationInsightsVersion = Config.getPropertyValue("applicationInsightsVersion", this)
        return sh(script: "cp /tmp/applicationinsights-agent-${applicationInsightsVersion}.jar service/lib/applicationinsights-agent.jar && docker build --build-arg SERVICE_VERSION=${version} --build-arg BASE_VERSION=${baseImage} -t ${dockerNameTag} -f service/Dockerfile service/.")
    } else if (serviceType == 'Node') {
        baseImage = Config.getPropertyValue("nodeBaseImage", this)
        return sh(script: "docker build --build-arg SERVICE_VERSION=${version} --build-arg BASE_VERSION=${baseImage} -t ${dockerNameTag} -f service/Dockerfile service/.")
    } else if (serviceType == 'JavaAzureFunction') {
        baseImage = Config.getPropertyValue("javaAzureFunctionBaseImage", this)
        return sh(script: "docker build --build-arg SERVICE_VERSION=${version} --build-arg BASE_IMAGE=${baseImage} -t ${dockerNameTag} -f service/Dockerfile service/.")
    } else if (serviceType == 'Stub') {
        baseImage =  Config.getPropertyValue("javaBaseImage", this)
        return sh(script: "docker build --build-arg SERVICE_VERSION=${version} --build-arg BASE_IMAGE=${baseImage} -t ${dockerNameTag} -f service/Dockerfile service/.")
    }
}
