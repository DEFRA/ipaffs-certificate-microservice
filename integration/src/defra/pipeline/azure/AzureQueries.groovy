package defra.pipeline.azure

import java.security.MessageDigest

import org.yaml.snakeyaml.Yaml
import defra.pipeline.config.Config

class AzureQueries {

    /**
     * Run a query and cache the results in a written file for future runs
     *
     * @param query   The query to run for az cli
     * @param reset   True if you want to refresh the result
     * @param script  The global script parameter
     */
    public static String runCachedQueryForAzCli(String query, boolean reset, Script script) {
        def fileName = "azurefiles/" + MessageDigest.getInstance("MD5").digest(query.bytes).encodeHex().toString()

        if (script.fileExists(fileName) && !reset) {
            return script.readFile(file:fileName)
        }

        script.echo "Calling azure and caching result"
        def output = script.sh(script: query, returnStdout: true )
        script.writeFile(file:fileName, text:output)
        return output
    }
    
     /**
     * Returns a list of repositories to be deleted eg feature repositories 
     *
     * @param containerRegistryName   The azure conatiner registry in ehich the repositories are located 
     * @param script                  The global script parameter
     * @param project                 Default configuration to 'imports' project
     * @param exclusions              Repositories to be ignored if not to be deleted
     */

    public static List getListOfReposToDelete(String containerRegistryName, Script script, String project, String exclusions) {

        //done to reduce azure calls
        //hardcode 'imports-release' on reverse grep to prevent deletion from config file deleting the repository
        def acrexclude = exclusions.replaceAll(',','|')
        def repoList = script.sh(script: "az acr repository list -n ${containerRegistryName} --output tsv | grep -Ei ${project} | grep -vwE 'imports-release|${acrexclude}'", returnStdout: true)
        def listOfACR = repoList.split() as List
        return listOfACR
    }

    /**
     * Returns a list of releae repositories for the imports project
     *
     * @param containerRegistryName   The azure conatiner registry to be queried
     * @param script                  The global script parameter
     */

    public static List getListOfReleaseRepositories(String containerRegistryName, Script script) {
      def releaseRepositoryList = script.sh(script: "az acr repository list -n ${containerRegistryName} --output tsv | grep 'imports-release'", returnStdout: true)
      def listOfRelaseRepos = releaseRepositoryList.split() as List
      return listOfRelaseRepos
    }

    /**
     * Returns a list of images located within a repository
     *
     * @param containerRegistryName   The azure conatiner registry to be queried 
     * @param repositoryName          The repository located within the registry to be queried
     * @param amountToReturn          The amount of queried images to be returned
     * @param script                  The global script parameter
     */

    public static List getListOfRepositoryImages(String containerRegistryName, String repositoryName, int amountToReturn, Script script) {
      def imagesList = script.sh(script: "az acr repository show-tags --name ${containerRegistryName} --repository ${repositoryName} --orderby time_desc --top ${amountToReturn} -o tsv", returnStdout: true)
      def listOfImages = imagesList.split() as List
      return listOfImages
    }

    /**
     * Get the docker image for a deployed docker container given a resource group and service name
     *
     * @param resourceGroupName  The name of the resource group to find deployed container of
     * @param serviceName        The service to find deployed container of
     * @param script             The global script parameter
     * @return The deployed containers image if found or null
     */
    public static String getContainerNameOfDeployedService(String resourceGroupName, String serviceName, Script script) {

        // Yaml as jsonslurper & classic caused issues, even with nocps
        def fileIn = script.sh(script: "az deployment group list --resource-group ${resourceGroupName} --query \"[?contains(name, '${serviceName}')].properties.parameters.dockerCustomImageName.value\" --output yaml", returnStdout: true )
        Yaml yaml = new Yaml()
        def yamlCurrentDeployed = yaml.load(fileIn);
        if (!yamlCurrentDeployed) {
            return null
        }
        return yamlCurrentDeployed[0]

    }

    /**
     * Get the latest master release version for a service
     *
     * @param serviceName   The service to find the latest version for
     * @param script        The global script parameter
     * @return The version number of the latest release
     */
    public static String getServiceVersionForLatestMasterRelease(String serviceName, Script script) {

        def containerRegistryName = Config.getPropertyValue("azureSndContainerRepository", script)
        def repositoryProjectPrefix = Config.getPropertyValue("azureContainerRepositoryReleasePrefix", script)

        String masterServiceVersion = null
        if (checkHasPublishedVersion(serviceName, repositoryProjectPrefix, script)) {
            masterServiceVersion = script.sh(script: "az acr repository show-tags -n ${containerRegistryName} --repository \"${repositoryProjectPrefix}/${serviceName}\" --detail --orderby time_desc --top 1 --query [].name -o tsv", returnStdout: true).trim().toString()
        }

        return masterServiceVersion
    }

    private static boolean checkHasPublishedVersion(String serviceName, String repositoryProjectPrefix, Script script) {
        def containerRegistryName = Config.getPropertyValue("azureSndContainerRepository", script)

        def listOfRepos = runCachedQueryForAzCli("az acr repository list -n ${containerRegistryName} --output tsv", false, script).split() as List
        def repoToFind = "${repositoryProjectPrefix}/${serviceName}"
        return listOfRepos.find { it == repoToFind }
    }

}
