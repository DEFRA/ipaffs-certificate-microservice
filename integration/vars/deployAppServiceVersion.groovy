import defra.pipeline.deploy.DeployActions

def call(String resourceGroupName, String serviceName, String subscription, Boolean useLatest = true, Script script) {
    DeployActions.deployService(resourceGroupName, serviceName, subscription, useLatest, this)
}
