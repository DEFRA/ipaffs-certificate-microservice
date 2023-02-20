import defra.pipeline.azure.AzureActions

def call(String resourceGroupName, String environment) {
    AzureActions.deployApplicationInsightsComponent(resourceGroupName, "application-insights", environment, this)
}
