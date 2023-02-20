import defra.pipeline.azure.AzureActions
import defra.pipeline.deploy.DeployQueries

def call(String subscription1, String subscription2) {

    def parallelCompare = [:]

    def deployableComponents = DeployQueries.getCollatedListOfDeployableComponents(subscription1, this)
    deployableComponents.each { collatedValidationArray ->
        collatedValidationArray.each { componentName ->
            parallelCompare[componentName] = {
                AzureActions.compareArmParameters(componentName, subscription1, subscription2, this)
            }
        }
        parallel parallelCompare
        parallelCompare = [:]
    }
}