import defra.pipeline.names.DockerName

def call(String serviceName, String subscription, String versionNew, String versionCurrent) {
    call(serviceName, subscription, subscription, versionNew, versionCurrent)
}

def call(String serviceName, String subscription, String subscriptionNew, String versionNew, String versionCurrent) {

    if (!fileExists('configuration/Dockerfile')) {
        echo "No configuration dockerfile, configuration container will be skipped"
        return
    }

    def currentServiceTag = DockerName.getConfigurationNameAndTag("${BRANCH_NAME}", serviceName, subscription, versionCurrent, false, this)
    def newServiceTag = DockerName.getConfigurationNameAndTag("${BRANCH_NAME}", serviceName, subscriptionNew, versionNew, true, this)

    sh(script: "docker tag ${currentServiceTag} ${newServiceTag}")
    sh(script: "docker push ${newServiceTag}")
}
