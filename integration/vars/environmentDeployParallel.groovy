import defra.pipeline.deploy.DeployActions
import defra.pipeline.deploy.DeployQueries

def call(String resourceGroupName, String serviceName, String environment, String branchName, String serviceVersion, Boolean useLatest = true) {

    deployAppInsights(resourceGroupName, environment)
    deployAppServicePlan(resourceGroupName, environment)

    def parallelDeployments = [:]

    def deployableComponents = DeployQueries.getCollatedListOfDeployableComponents(environment, this)
    deployableComponents.each { collatedDeploymentArray ->
        collatedDeploymentArray.each { componentName ->
            parallelDeployments[componentName] = {
                DeployActions.deployParallelComponent(resourceGroupName, environment, branchName, serviceVersion, serviceName, componentName, useLatest, this)
            }
        }
        parallel parallelDeployments
        parallelDeployments = [:]
    }

    def brokenDeployments = DeployQueries.getBrokenDeployedComponents(resourceGroupName, this)
    DeployActions.redeployBrokenServices(brokenDeployments, resourceGroupName, serviceName, environment, branchName, serviceVersion, this)
}
