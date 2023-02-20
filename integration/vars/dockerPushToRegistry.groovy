import defra.pipeline.names.DockerName

def call(String serviceName, String subscription, String version) {
  return call(serviceName, "${BRANCH_NAME}", false, subscription, version)
}

def call(String serviceName, String branchName, boolean isMasterRelease, String subscription, String version) {
  def dockerNameTag = DockerName.getNameAndTag(branchName, serviceName, subscription, version, isMasterRelease, this)
  return sh(script: "docker push ${dockerNameTag} 2>&1 | tail -5")
}
