package defra.pipeline.environments

import defra.pipeline.config.Config

class EnvironmentQueries {

    private static String buildPoolTagKey = "BuildPool"

    /**
     * Get all currently built pools
     *
     * @param script                   The global script parameter
     * @return String                  The pool reserved on success and null when none are available
     */
    public static List getAllPools(Script script) {
        def poolResourceGroupPoolPrefix = Config.getPropertyValue("azureResourceGroupPoolPrefix", script)
        def resourceGroupNames = script.sh(script: "az group list --query \"[?contains(name, '${poolResourceGroupPoolPrefix}')].name\" -o tsv", returnStdout: true)
        return resourceGroupNames.split() as List
    }

    /**
     * Get all currently built hotfix pools
     *
     * @param script                   The global script parameter
     * @return String                  The pool reserved on success and null when none are available
     */
    public static List getAllHotfixPools(Script script) {
        def hotfixPoolResourceGroupPoolPrefix = Config.getPropertyValue("azureResourceGroupHotfixPoolPrefix", script)
        def resourceGroupNames = script.sh(script: "az group list --query \"[?contains(name, '${hotfixPoolResourceGroupPoolPrefix}')].name\" -o tsv", returnStdout: true)
        return resourceGroupNames.split() as List
    }

    /**
     * Get all free pools, that is those pools that aren't tagged with the value specified
     *
     * @param script                   The global script parameter
     * @return String                  The pool reserved on success and null when none are available
     */
    public static List getFreePools(Script script, boolean refresh = false) {
        def freePoolTagValue = Config.getPropertyValue("freePoolTagValue", script)
        def poolResourceGroupPoolPrefix = Config.getPropertyValue("azureResourceGroupPoolPrefix", script)
        def freePoolResourceGroups = script.sh(script: "az group list --query \"[?contains(name, '${poolResourceGroupPoolPrefix}')].name\" --tag ${buildPoolTagKey}=${freePoolTagValue} -o tsv", returnStdout: true).split() as List
        return freePoolResourceGroups
    }

    /**
     * Check if a pool is already in use for the specified service and tag
     *
     * @param serviceName              The name of the microservice being built
     * @param tagName                  The tag name to check for
     * @param script                   The global script parameter
     * @return String                  Returns the resource group name if a pool is already in use, or null otherwise
     */
    public static String existingPoolWithTag(String serviceName, String tagName, Script script) {
        def poolResourceGroupPoolPrefix = Config.getPropertyValue("azureResourceGroupPoolPrefix", script)
        def poolsWithTag = script.sh(script: "az group list --query \"[?contains(name, '${poolResourceGroupPoolPrefix}')].name\" --tag ${buildPoolTagKey}=${serviceName}:${tagName} -o tsv", returnStdout: true).split() as List

        if (poolsWithTag.size() != 0) {
            return poolsWithTag[0]
        }
        return null
    }

    /**
     * Get all resource groups
     *
     * @param script  The global script parameter
     * @return List   All resource groups
     */
    public static List getAllResourceGroups(Script script) {
        def resourceGroupNames = script.sh(script: "az group list --query [].name -o tsv", returnStdout: true)
        return resourceGroupNames.split() as List
    }

    /**
     * Find the id of the next pool that doesn't exist.
     *
     * For example when we have pools 1, 2, 4 and 5 that exist it will return pool 3.
     *
     * @param script                   The global script parameter
     * @return String                  The pool reserved on success and null when none are available
     */
    public static int getNextIdForPoolCreation(Script script) {
        def poolResourceGroupPoolPrefix = Config.getPropertyValue("azureResourceGroupPoolPrefix", script)
        def allPools = this.getAllPools(script)

        def id = 1
        while (true) {
            def pool = "${poolResourceGroupPoolPrefix}${id}" as String
            if (!allPools.contains(pool)) {
                break
            }
            id += 1
        }

        return id
    }

}

