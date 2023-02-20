import defra.pipeline.azure.AzureActions
import defra.pipeline.azure.AzureQueries
import defra.pipeline.config.Config

def call(String subscription) {
    AzureQueries acrQueries = new AzureQueries()
    def acrName = Config.getPropertyValue("azureSndContainerRepository", this)
    def allACRReleaseRepos = acrQueries.getListOfReleaseRepositories(acrName, this)

    for ( releaseRepositories in allACRReleaseRepos) {
        def listOfAllImages = acrQueries.getListOfRepositoryImages(acrName, releaseRepositories, 1000, this)
        def listOftop70Images = acrQueries.getListOfRepositoryImages(acrName, releaseRepositories, 70, this)
        def listOftop50Images = acrQueries.getListOfRepositoryImages(acrName, releaseRepositories, 50, this)
        def listOftop20Images = acrQueries.getListOfRepositoryImages(acrName, releaseRepositories, 20, this)
        def imagesToBeRemoved = listOfAllImages - listOftop70Images

        AzureActions azureActions = new AzureActions()
        if(imagesToBeRemoved.size() > 0) {
            for(dockerImage in imagesToBeRemoved) {
                def imageName = releaseRepositories + ':' + dockerImage
                azureActions.destroyACR(acrName, 'image', this, imageName)
            }
        } else {
            echo "No Images Found for deletion in ${releaseRepositories}"
        }
    }
}
