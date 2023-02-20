
import defra.pipeline.azure.AzureActions
import defra.pipeline.azure.AzureQueries
import defra.pipeline.config.Config

def call(String subscription, String doNotDelete) {
    AzureQueries acrQueries = new AzureQueries()
    def acrName = Config.getPropertyValue("azureSndContainerRepository", this)
    def allACRReposToRemove = acrQueries.getListOfReposToDelete(acrName, this, "imports-", doNotDelete)
    println allACRReposToRemove
    AzureActions azureActions = new AzureActions()

    if(allACRReposToRemove.size() > 0) {
        for (acrRepoNames in allACRReposToRemove) {
            azureActions.destroyACR(acrName, 'repository', this, acrRepoNames)
        }
    } else {
            echo "No ACR Repos Found for deletion in ${acrName}"
    }   
}
