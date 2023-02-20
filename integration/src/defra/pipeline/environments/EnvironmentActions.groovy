package defra.pipeline.environments

import defra.pipeline.azure.AzureActions
import defra.pipeline.azure.AzureQueries
import defra.pipeline.azure.search.AzureSearchActions
import defra.pipeline.config.Config
import defra.pipeline.database.DatabaseQueries
import defra.pipeline.database.DatabaseActions
import defra.pipeline.deploy.DeployQueries

class EnvironmentActions {

    /**
     * Create a new pool
     *
     * @throws EnvironmentMaxPoolsLimitException  If we have hit the limit on number of pools
     * @return The pool created on success or null on failure
     */
    public static String createPool(Script script) {

        def nextPoolId = EnvironmentQueries.getNextIdForPoolCreation(script)

        script.echo("nextPoolId=${nextPoolId}")

        if (nextPoolId > Config.getPropertyValue("maxNumPoolsLimit", script).toInteger()) {
            throw new EnvironmentMaxPoolsLimitException()
        }

        def poolResourceGroupNamePrefix = Config.getPropertyValue("azureResourceGroupPoolPrefix", script)
        def resourceGroupName = "${poolResourceGroupNamePrefix}${nextPoolId.toString()}"

        script.echo("resourceGroupName=${resourceGroupName}")

        if (!createResourceGroup(resourceGroupName, script)) {
            return null
        }

        return resourceGroupName

    }

    /**
     * Create a resource group with name specified.
     *
     * @param resourceGroupName        Name of the resource group
     * @return The resource group created on success or null on failure
     */
    public static boolean createResourceGroup(String resourceGroupName, Script script) {

        script.echo("Creating Resource Group ${resourceGroupName}")

        try {
            AzureActions.createResourceGroup(resourceGroupName, script)
            AzureActions.assignRoles(resourceGroupName, script)
            AzureActions.assignTags(resourceGroupName, script)
        } catch (Exception e) {
            AzureActions.deleteResourceGroup(resourceGroupName, script)
            return false
        }

        return true

    }

    /**
     * Find a free pool and reserve it with retrying.
     *
     * @param serviceName              The name of the microservice being built
     * @param reservationTag           The tag to reserve an environment with
     * @param retryCount               Number of times to retry
     * @param sleepTime                Number of seconds to wait before retry
     * @param script                   The global script parameter
     * @throws EnvironmentNoPoolsFreeException  If we have hit the maximum number of retries
     * @return The pool reserved
     */
    public static String reserveFreePool(String serviceName, String reservationTag, int retryCount, int sleepTime, Script script) {

        def reservedResourceGroup

        def attempt = 1

        while (true) {
            reservedResourceGroup = reserveFreePool(serviceName, reservationTag, script)
            if (reservedResourceGroup) {
                return reservedResourceGroup
            }

            attempt += 1
            if (attempt > retryCount) {
                throw new EnvironmentNoPoolsFreeException()
            }

            script.sleep sleepTime
        }

    }

    /**
     * Find a free pool and reserve it.
     *
     * @param serviceName              The name of the microservice being built
     * @param reservationTag           The tag to reserve an environment with
     * @param script                   The global script parameter
     * @return The pool reserved on success and null when none are available
     */
    public static String reserveFreePool(String serviceName, String reservationTag, Script script) {

        def freePools = EnvironmentQueries.getFreePools(script, true)
        script.echo "Free pools: ${freePools}"
        if (freePools.size() == 0) {
            return null
        }

        tagResourceGroup(freePools[0], serviceName, reservationTag, script)

        return freePools[0]
    }

    /**
     * Tag a specific resource group's BuildPool.
     *
     * @param resourceGroupName  The resource group name to tag
     * @param serviceName        The name of the microservice being built
     * @param tagName            The tag name
     * @param script             The global script parameter
     */
    public static void tagResourceGroup(String resourceGroupName, String serviceName, String tagName, Script script) {

        def cmd = "az group update --name ${resourceGroupName} --set tags.BuildPool=\"${serviceName}:${tagName}\""
        def out
        try {
            out = script.sh(script: cmd, returnStdout: true)
        } catch (Exception e) {
            script.echo("""Error: ${out}""")
            throw e
        }
        script.echo "Successfully ran cmd: ${cmd}"

    }

    /**
     * Destroy a resource group including all databases
     *
     * @param resourceGroupName           The resource group name to destroy
     * @param destroyAssociatedDatabases  Whether to destroy associated databases
     * @param script                      The global script parameter
     */
    public static void destroyResourceGroup(String resourceGroupName, boolean destroyAssociatedDatabases, String subscription, boolean wait = true, Script script) {

        tagResourceGroup(resourceGroupName, "", "destroying-pool", script)

        if (destroyAssociatedDatabases) {
            def componentNamesToDeploy = DeployQueries.getListOfDeployableComponents(Config.getPropertyValue("${subscription.toLowerCase()}DeploymentList", script), script)
            for (componentName in componentNamesToDeploy) {
                if (DeployQueries.hasDatabase(componentName, subscription, script)) {
                    DatabaseActions.dropDatabase(componentName, resourceGroupName, script)
                }
                if (DeployQueries.hasConfiguration(componentName, subscription, script)) {
                    AzureSearchActions azureSearchActions = new AzureSearchActions(resourceGroupName, componentName, subscription, script)
                    azureSearchActions.deleteSearchSchema()
                }
            }
        }

        AzureActions.deleteResourceGroup(resourceGroupName, wait, script)

    }

}
