/**
 * Release or upgrade the pool specified to the latest version of release master
 *
 * @param resourceGroupName  The resource group to sync
 * @param subscription       The subscription, e.g. Sandpit
 * @param serviceToSkip      Optional service that shouldn't be synced
 */

import defra.pipeline.config.Config
import defra.pipeline.database.DatabaseQueries
import defra.pipeline.azure.AzureActions
import defra.pipeline.deploy.DeployActions
import defra.pipeline.names.PoolTag
import defra.pipeline.environments.EnvironmentActions
import defra.pipeline.environments.EnvironmentQueries

def call(String resourceGroupName, String subscription) {
    call(resourceGroupName, subscription, "", true)
}

def call(String resourceGroupName, String subscription, String serviceToSkip) {
    call(resourceGroupName, subscription, serviceToSkip, true)
}

def call(String resourceGroupName, String subscription, String serviceToSkip, boolean resetDatabaseIfDeployed) {
//TODO: fix so colour is output when in this block so can spot reset, rename TO environmentUpdateToMasterReleases
    ansiColor('xterm') {
        echo "----RESETTING ${resourceGroupName} TO MASTER STATE---"
        try {

            DeployActions.deployAllServices(resourceGroupName, subscription, [serviceToSkip], this)

        } catch (e) {
            echo "DEPLOY FAILED"
            echo "${e.getMessage()}"
            echo "${e.getCause()}"
            echo "${e.getStackTrace()}"

            if (resourceGroupName in EnvironmentQueries.getAllPools(this)) {
                echo "REMOVING RESOURCE GROUP"
                EnvironmentActions.destroyResourceGroup(resourceGroupName, resetDatabaseIfDeployed, subscription, true, this)
            }
            throw e
        }
        echo "----FINISHED RESETTING ${resourceGroupName} TO MASTER STATE---"
    }
}
