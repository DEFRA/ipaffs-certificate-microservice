package defra.pipeline.deploy

import defra.pipeline.config.Config
import defra.pipeline.names.PoolTag

class DeployQueries {

    /**
     * Get a list of all deployable services
     *
     * @param script  The global script parameter
     * @return List of deployable services
     */
    public static List<String> getListOfDeployableComponents(String deploymentListFile, Script script) {

        def deployableComponentsDetails = getComponentDetails(deploymentListFile, script)

        return deployableComponentsDetails.collect { x -> x.serviceName }
    }

    /**
     * Get a table of any failed services in a resource group
     *
     * @param resourceGroupName  The resource group name
     * @param script             The global script parameter
     * @return Table of deployed services with Failed provisioningState
     */
    public static String getBrokenDeployedComponents(String resourceGroupName, Script script) {
        def getListOfDeployments = "az deployment group list --resource-group ${resourceGroupName} -o table"
        script.echo("=======State of deployment=======")
        def listOfDeployments = script.sh(script: getListOfDeployments, returnStdout: true )
        script.echo("$listOfDeployments")

        def getBrokenListOfDeployments = "az deployment group list --resource-group ${resourceGroupName} --query \"[?properties.provisioningState=='Failed'].{name:name}\" -o table"
        def brokenDeployments = script.sh(script: getBrokenListOfDeployments, returnStdout: true )

        return brokenDeployments
    }

    /**
     * Get a list of all deployable services, collated by desired number of parallel deployable services
     *
     * @param script  The global script parameter
     * @return Collated list of deployable services
     */
    public static List<List<String>> getCollatedListOfDeployableComponents(String subscription, Script script) {

        def deployableComponentList = getListOfDeployableComponents(Config.getPropertyValue("${subscription.toLowerCase()}DeploymentList", script), script)

        return deployableComponentList.collate(Config.getPropertyValue("maxNumberOfParallelDeployments", script).toInteger())
    }

    /**
     * Check whether a service name should have a database, defaults to true if service not found
     *
     * @param serviceName The name of the service
     * @param subscription The subscription of deployment
     * @param script      The global script parameter
     * @return List of deployable services
     */
    public static boolean hasDatabase(String serviceName, String subscription, Script script) {

        def component = findComponent(Config.getPropertyValue("${subscription.toLowerCase()}DeploymentList", script), serviceName, script)

        if (!component) {
            return hasConfiguration(serviceName, subscription, script)
        }

        return component.hasDatabase

    }

    /**
     * Check whether a service name should have a configuration, defaults to true if service not found
     *
     * @param serviceName The name of the service
     * @param script      The global script parameter
     * @return List of deployable services
     */
    public static boolean hasConfiguration(String serviceName, String subscription, Script script) {

        def component = findComponent(Config.getPropertyValue("${subscription.toLowerCase()}DeploymentList", script), serviceName, script)

        if (!component) {
            return script.fileExists("configuration/Dockerfile")
        }

        return component.hasConfiguration

    }

    /**
     * Check whether a service name has a release number associated under that subscription and return it
     *
     * @param serviceName   The name of the service
     * @param subscription  The subscription
     * @param script        The global script parameter
     * @return release number if it is given or null
     */
    public static String getReleaseNumber(String subscription, String serviceName, Script script) {

        def component = findComponent(Config.getPropertyValue("${subscription.toLowerCase()}DeploymentList", script), serviceName, script)

        if ('releaseNumber' in component) {
            return component['releaseNumber']
        }

        return null

    }

    /**
     * Check the release type, either release or hotfix expected
     *
     * @param serviceName   The name of the service
     * @param subscription  The subscription
     * @param script        The global script parameter
     * @return release number if it is given or null
     */
    public static String getReleaseType(String subscription, String serviceName, Script script) {

        def component = findComponent(Config.getPropertyValue("${subscription.toLowerCase()}DeploymentList", script), serviceName, script)

        if (!('releaseType' in component)) {
            return null
        }

        if (!(component['releaseType'] in ['release', 'hotfix'])) {
            return null
        }

        return component['releaseType']

    }

     /**
     * Returns HealthCheckStatus to indicate state
     *
     * @param serviceName        The name of the service
     * @param resourceGroupName  The name of the resource group
     * @param subscription       The subscription
     * @param script             Jenkins reference
     * @return HealthCheckStatus Enum
     */
    public static String checkHealthCheck(String serviceName, String resourceGroupName, String subscription, Script script) {

        script.echo "Performing health check for ${serviceName}"

        def parametersMap = Config.getParameters(serviceName, subscription.toLowerCase(), script)
        def dnsSuffix = (resourceGroupName.startsWith("SNDIMPINFRGP")) ? '.azurewebsites.net' : '.imp.snd.azure.defra.cloud'
        def fullServiceName = (serviceName.contains('proxy')) ? parametersMap.parameters.serviceName.value : PoolTag.getNameWithTag(serviceName, resourceGroupName)

        def healthCheckStatus = HealthCheckStatus.UNKNOWN_STATE

        def healthUrl = "https://${fullServiceName}${dnsSuffix}/admin/health-check"
        try {
            def healthCheck = script.sh(script: "set +x; curl -sk -m 10 -o /dev/null -w \"%{http_code}\" ${healthUrl}", returnStdout: true)

            if (healthCheck == '404') {
                healthCheckStatus = HealthCheckStatus.UNSUPPORTED_NO_ENDPOINTS
            }
            else if (healthCheck == '500'){
                healthCheckStatus = HealthCheckStatus.ENDPOINTS_FAILED
            } else if(healthCheck == '000') {
                healthCheckStatus = HealthCheckStatus.NOT_DEPLOYED
            }
            else if(healthCheck == '503') {
                healthCheckStatus = HealthCheckStatus.DOWN
            } else if (healthCheck == '200') {
                healthCheckStatus = HealthCheckStatus.UP
            }
            else {
                script.echo "Unrecognised status code for health check: ${healthCheckStatus}"
            }

        } catch (Exception ex) {
            script.echo 'Health Check unexpected error'
            script.echo ex.getMessage()
        }

        script.echo "Health check result: ${healthCheckStatus}"

        return healthCheckStatus
    }

    /**
     * Returns currently running service version
     *
     * @param serviceName The name of the service
     * @param resourceGroupName      The name of the resource group
     * @param subscription      The subscription
     * @param script      Jenkins reference
     * @return service version on a pool for given service
     */
    public static String getCurrentRunningVersion(String serviceName, String resourceGroupName, String subscription, Script script) {

        def parametersMap = Config.getParameters(serviceName, subscription.toLowerCase(), script)
        def dnsSuffix = (resourceGroupName.startsWith("SNDIMPINFRGP")) ? '.azurewebsites.net' : '.imp.snd.azure.defra.cloud'
        def fullServiceName = (serviceName.contains('proxy')) ? parametersMap.parameters.serviceName.value : PoolTag.getNameWithTag(serviceName, resourceGroupName)

        def infoURL = "https://${fullServiceName}${dnsSuffix}/admin/info"
        script.echo "Attempting to get: ${infoURL}"

        try {
            def deployedVersion = script.sh(script: "set +x; curl -sk -m 10 ${infoURL} | jq -rj .app.version", returnStdout: true)
            return deployedVersion
        } catch (Exception ex) {
            script.echo 'Failed to access info endpoint'
            script.echo ex.getMessage()
            return ""
        }

    }

    /**
     * Get a list of dictionaries containing serviceName, hasDatabase and hasConfiguration
     *
     * @param script  The global script parameter
     * @return List of deployable services
     */
    private static List<LinkedHashMap> getComponentDetails(String deploymentListFile, Script script) {

        def ret = []

        def deployableComponentsDetailsData = script.libraryResource deploymentListFile

        for (deployableComponentDetailsData in deployableComponentsDetailsData.split('\n')) {
            def deployableComponentsDetails = deployableComponentDetailsData.tokenize(':')

            def serviceDetails = ['serviceName': deployableComponentsDetails[0],
                                  'hasDatabase': deployableComponentsDetails[1] == 'has_database',
                                  'hasConfiguration': deployableComponentsDetails[2] == 'has_configuration']

            if (deployableComponentsDetails.size() == 5) {
                serviceDetails['releaseNumber'] = deployableComponentsDetails[3]
                serviceDetails['releaseType'] = deployableComponentsDetails[4]
            }

            ret.add(serviceDetails)
        }

        return ret

    }

    /**
     * Get the dictionary for a particular service containing serviceName, hasDatabase and hasConfiguration
     *
     * @param serviceName The name of the service
     * @param script      The global script parameter
     * @return List of deployable services
     */
    private static LinkedHashMap findComponent(String deploymentListFile, String serviceName, Script script) {

        def deployableComponentsDetails = getComponentDetails(deploymentListFile, script)

        return deployableComponentsDetails.find { it.serviceName == serviceName }

    }

}
