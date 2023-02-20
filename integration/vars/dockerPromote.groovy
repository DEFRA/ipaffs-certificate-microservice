import defra.pipeline.names.DockerName

def call(String serviceName, String subscription, String versionNew, String versionCurrent) {
  call(serviceName, subscription, subscription, versionNew, versionCurrent, false)
}

def call(String serviceName, String subscription, String subscriptionNew, String versionNew, String versionCurrent, Boolean requiresDockerPull) {
    
  def currentServiceTag = ""
  def newServiceTag = ""

  if(subscription == 'Sandpit-Hotfix' && requiresDockerPull == true){
    currentServiceTag = DockerName.getNameAndTag('hotfix', serviceName, 'Sandpit', versionCurrent, true, this)
    newServiceTag = DockerName.getNameAndTag("${BRANCH_NAME}", serviceName, subscriptionNew, versionNew, true, this)
    sh(script: "docker pull ${currentServiceTag}")
  } else if(requiresDockerPull == true) {
    currentServiceTag = DockerName.getNameAndTag("${BRANCH_NAME}", serviceName, subscription, versionCurrent, true, this)
    newServiceTag = DockerName.getNameAndTag("${BRANCH_NAME}", serviceName, subscriptionNew, versionNew, true, this)
    sh(script: "docker pull ${currentServiceTag}")
  } else {
    currentServiceTag = DockerName.getNameAndTag("${BRANCH_NAME}", serviceName, subscription, versionCurrent, false, this)
    newServiceTag = DockerName.getNameAndTag("${BRANCH_NAME}", serviceName, subscriptionNew, versionNew, true, this)
  }

  sh(script: "docker tag ${currentServiceTag} ${newServiceTag}")
  sh(script: "docker push ${newServiceTag}")
}