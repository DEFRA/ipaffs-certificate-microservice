import defra.pipeline.azure.AzureActions

def call(String resourceGroupName, String environment) {
    call(resourceGroupName, 'services-general', environment)
    call(resourceGroupName, 'services-general-2', environment)
    call(resourceGroupName, 'services-general-3', environment)
}

def call(String resourceGroupName, String appServicePlanGroup, String environment) {
    AzureActions.deployAppServicePlanComponent(resourceGroupName, appServicePlanGroup, environment, this)
}
