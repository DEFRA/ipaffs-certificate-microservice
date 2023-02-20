import defra.pipeline.environments.EnvironmentActions

def call(String resourceGroupName, boolean destroyDatabases, String subscription) {
    EnvironmentActions.destroyResourceGroup(resourceGroupName, destroyDatabases, subscription, true, this)
}

