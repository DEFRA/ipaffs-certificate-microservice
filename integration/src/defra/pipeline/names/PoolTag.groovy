package defra.pipeline.names

class PoolTag {

    /**
     * Get the name of the microservice in the pool
     *
     * @param serviceName        The name of the microservice
     * @param resourceGroupName  The name of the resource group to create
     * @param script             The global script parameter
     * @return Full service name of microservice in the pool. e.g. `notification-microservice-1`.
     */
    public static String getNameWithTag(String serviceName, String resourceGroupName) {
        def fullName = serviceName

        def poolId = getId(resourceGroupName)
        if (poolId && !suffixWouldBeDuplicate(poolId, serviceName))
        {
            fullName += "-${poolId}"
        }

        return fullName
    }

    public static String getId(String resourceGroupName) {
        def poolID = ''

        def listTags = resourceGroupName.tokenize("-")

        if (listTags.size() > 1)
        {
            poolID = "${listTags[listTags.size()-1]}"
        }

        return poolID
    }

    /**
     * Used to prevent resource groups being generated with duplicate names, e.g. imports-proxy-feature-feature
     *
     * @param nameToAppend       The suffixed that would be appended
     * @param serviceName        The name of the service
     * @return True if suffix would be duplicated, false if not.
     */
    private static boolean suffixWouldBeDuplicate(String nameToAppend, String serviceName) {
        boolean wouldBeDoubleName = false
        def serviceNameSplit = serviceName.tokenize("-")
        if (serviceNameSplit.size() > 1){
            String endOfServiceName = serviceNameSplit[serviceNameSplit.size()-1]
            if (endOfServiceName == nameToAppend) {
                wouldBeDoubleName = true
            }
        }
        return wouldBeDoubleName
    }
}
