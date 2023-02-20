import defra.pipeline.azure.AzureActions
import defra.pipeline.deploy.DeployQueries

def call(String environment) {

    def parallelValidate = [:]

    def deployableComponents = DeployQueries.getCollatedListOfDeployableComponents(environment, this)
    deployableComponents.each { collatedValidationArray ->
        collatedValidationArray.each { componentName ->
            parallelValidate[componentName] = {
                AzureActions.validateArmTemplate(environment, componentName, this)
            }
        }
        parallel parallelValidate
        parallelValidate = [:]
    }
}
