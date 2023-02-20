package defra.pipeline.deploy

import defra.pipeline.azure.AzureActions
import defra.pipeline.azure.AzureQueries
import defra.pipeline.azure.search.AzureSearchActions
import defra.pipeline.config.Config
import defra.pipeline.database.DatabaseActions
import defra.pipeline.environments.EnvironmentQueries
import defra.pipeline.names.Branches
import defra.pipeline.names.DockerName
import defra.pipeline.names.PoolTag
import defra.pipeline.script.ScriptActions
import defra.pipeline.vault.VaultKey

class DeployActions {

    /**
     * Deploy, update or redeploy every deployable service on a resource group to either the latest
     * master releases or current branch builds service and version, including creating DBs and running
     * configuration as necessary.
     *
     * @param resourceGroupName  The name of the resource group to release to
     * @param subscription       The subscription, e.g. Sandpit
     * @param branchName         The name of the branch
     * @param serviceVersion     The version of the deployed service
     * @param serviceName        The name of the service the branch is building
     * @param componentName      The name of the component to deploy
     * @param script             The global script parameter
     */
    public static deployParallelComponent(String resourceGroupName, String subscription, String branchName, String serviceVersion, String serviceName, String componentName, Boolean useLatest = true, Script script) {
        if (serviceName == componentName) {
            def projectName = Config.getPropertyValue("projectName", script)

            String containerRepository = "${projectName.toLowerCase()}-" + Branches.getBranchPrefix(branchName)
            String dockerNameTag = DockerName.getNameAndTag(branchName, serviceName, subscription, serviceVersion, false, script)

            deployServiceWithDockerImage(resourceGroupName, serviceName, subscription, containerRepository, dockerNameTag, serviceVersion, script)
        } else {
            deployService(resourceGroupName, componentName, subscription, useLatest, [serviceName], script)
        }
    }

    /**
     * Deploy, update or redeploy every deployable service on a resource group to the latest
     * master releases, including creating DBs and running configuration as necessary,
     * skipping supplied services
     *
     * @param resourceGroupName  The name of the resource group to release to
     * @param subscription       The subscription, e.g. Sandpit
     * @param servicesToSkip     A list of service names not to deploy
     * @param script             The global script parameter
     */
    public static void deployAllServices(String resourceGroupName, String subscription, List<String> servicesToSkip, Script script) {

        if (resourceGroupName.startsWith("SNDIMPINFRGP001")) {
            AzureActions.deployApplicationInsightsComponent(resourceGroupName, "application-insights", subscription, script)
        }

        if ((resourceGroupName != "SNDIMPINFRGP001-imports-static-vnet") && resourceGroupName.startsWith("SNDIMPINFRGP")) {
            AzureActions.deployAppServicePlanComponent(resourceGroupName, "services-general", subscription, script)
            AzureActions.deployAppServicePlanComponent(resourceGroupName, "services-general-2", subscription, script)
            AzureActions.deployAppServicePlanComponent(resourceGroupName, "services-general-3", subscription, script)
        } else {
            def listOfAppServiceplans = Config.getPropertyValue("aseAppServicePlanList", script).tokenize(',[]')
            for (appServicePlan in listOfAppServiceplans) {
                AzureActions.deployAppServicePlanComponent(resourceGroupName, "${appServicePlan}", subscription, script)
            }
        }

        for (serviceName in DeployQueries.getListOfDeployableComponents(Config.getPropertyValue("${subscription.toLowerCase()}DeploymentList", script), script)) {
            if (serviceName in servicesToSkip) {
                script.echo "Not deploying component for ${serviceName}"
                continue
            }
            deployService(resourceGroupName, serviceName, subscription, script)
        }
    }

    /**
     * Deploy, update or redeploy a service to the latest master release including creating DBs and
     * running configuration as necessary
     *
     * @param resourceGroupName The name of the resource group to release to
     * @param serviceName       The service to deploy
     * @param subscription      The subscription, e.g. Sandpit
     * @param useLatest         Switch to select latest or release version from deployList file
     * @param script            The global script parameter
     */
    public static void deployService(String resourceGroupName, String serviceName, String subscription, Boolean useLatest = true, Script script) {

        script.echo "Checking for service: ${serviceName}"
        def releaseVersion
        def releaseType

        if (useLatest) {
            releaseVersion = AzureQueries.getServiceVersionForLatestMasterRelease(serviceName, script)
        } else {
            releaseVersion = DeployQueries.getReleaseNumber("octopus", serviceName, script)
            releaseType = DeployQueries.getReleaseType("octopus", serviceName, script)
        }

        if (releaseVersion == "" || releaseVersion == null) {
            script.echo "No published release for this service - not releasing"
            return
        }

        def runningContainerVersion = DeployQueries.getCurrentRunningVersion(serviceName, resourceGroupName, subscription, script)
        def healthCheckStatus = DeployQueries.checkHealthCheck(serviceName, resourceGroupName, subscription, script)

        if (healthCheckStatus != HealthCheckStatus.UP && healthCheckStatus != HealthCheckStatus.UNSUPPORTED_NO_ENDPOINTS) {
            script.echo "----HEALTH CHECK FOR ${serviceName} IS UNHEALTHY ON ${resourceGroupName}, REDEPLOYING SERVICE"
            executeDeployService(resourceGroupName, serviceName, subscription, releaseVersion, releaseType, script)
            return
        } else if (runningContainerVersion == "" || runningContainerVersion == null) {
            script.echo "----NO CURRENT RUNNING VERSION OF ${serviceName} FOUND ON ${resourceGroupName}, REDEPLOYING SERVICE"
            executeDeployService(resourceGroupName, serviceName, subscription, releaseVersion, releaseType, script)
            return
        } else if (serviceName == "rds-wiremock-microservice") {
            script.echo "----FORCING UPDATE OF ${serviceName} ON ${resourceGroupName}, REDEPLOYING SERVICE"
            executeDeployService(resourceGroupName, serviceName, subscription, releaseVersion, releaseType, script)
            return
        } else {
            script.echo "Container on Release is     : ${releaseVersion}"
            script.echo "Container currently running : ${runningContainerVersion}"

            if (runningContainerVersion == releaseVersion) {
                script.echo "Is currently running master release - not updating"
                return
            }

            script.echo "Running different version, deploying."

            executeDeployService(resourceGroupName, serviceName, subscription, releaseVersion, releaseType, script)
        }
    }

    /**
     *
     * @param resourceGroupName The name of the resource group to release to
     * @param serviceName       The service to deploy
     * @param subscription      The subscription, e.g. Sandpit
     * @param releaseVersion    The expected release version
     * @param script            The global script parameter
     */
    private static void executeDeployService(String resourceGroupName, String serviceName, String subscription, String releaseVersion, String releaseType, Script script) {
        def repositoryProjectPrefix

        if (releaseType == 'hotfix') {
            repositoryProjectPrefix = Config.getPropertyValue("azureContainerRepositoryHotfixPrefix", script)
        } else {
            repositoryProjectPrefix = Config.getPropertyValue("azureContainerRepositoryReleasePrefix", script)
        }

        def containerRegistry = Config.getPropertyValue("azureSndContainerRegistry", script)
        def fullContainerName = "${containerRegistry}/${repositoryProjectPrefix}/${serviceName}:${releaseVersion}"

        deployServiceWithDockerImage(resourceGroupName, serviceName, subscription, repositoryProjectPrefix, fullContainerName, releaseVersion, script)
    }

    /**
     * Deploy, update or redeploy a service to the latest master release including creating DBs and
     * running configuration as necessary
     *
     * @param resourceGroupName  The name of the resource group to release to
     * @param serviceName        The service to deploy
     * @param subscription       The subscription, e.g. Sandpit
     * @param script             The global script parameter
     */
    public static void deployService(String resourceGroupName, String serviceName, String subscription, Boolean useLatest = true, List<String> servicesToSkip, Script script) {

        if (serviceName in servicesToSkip) {
            script.echo "Not deploying component for ${serviceName}"
            return
        }
        deployService(resourceGroupName, serviceName, subscription, useLatest, script)
    }

    /**
     * Release a component to a resource given a docker image name, including creating a DB
     * and running the configuration scripts if appropriate
     *
     * @param resourceGroupName         The name of the resource group to release to
     * @param serviceName               The service to deploy
     * @param subscription              The subscription, e.g. Sandpit
     * @param containerRepository       The repository within azure container registry in which the image is stored
     * @param componentDockerImageName  The docker image of the main component
     * @param script                    The global script parameter
     */
    public static void deployServiceWithDockerImage(String resourceGroupName, String serviceName, String subscription, String containerRepository, String componentDockerImageName, String expectedVersion, Script script) {

        def ranConfig = false

        AzureSearchActions azureSearchActions = new AzureSearchActions(resourceGroupName, serviceName, subscription, script)

        if (DeployQueries.hasDatabase(serviceName, subscription, script)) {
            def createdDb = DatabaseActions.createDatabase(serviceName, resourceGroupName, script)

            def allPools = EnvironmentQueries.getAllPools(script)
            def isPool = resourceGroupName in allPools

            // On a development environment we try and reset the configuration to latest master before
            // running the branches extra bits. Of course on non-development environments we don't do
            // this.
            if (isPool && DeployQueries.hasConfiguration(serviceName, subscription, script)) {
                //Always clear out the azure schema if any, so master will auto reapply
                //and make sure the index matches the database
                azureSearchActions.deleteSearchSchema()

                //Reset the database if it's not a new one
                if (!createdDb) {
                    def dockerContainerVersion = AzureQueries.getServiceVersionForLatestMasterRelease(serviceName, script)
                    def repositoryProjectPrefix = Config.getPropertyValue("azureContainerRepositoryReleasePrefix", script)
                    def containerRegistry = Config.getPropertyValue("azureSndContainerRegistry", script)
                    def dockerImageName = "${containerRegistry}/${repositoryProjectPrefix}/${serviceName}:${dockerContainerVersion}"

                    // Try and drop everything and recreate from master liquibase, if that
                    // fails we have to drop the DB and start again. This is because we
                    // don't know what status we are in from liquibase previous runs.
                    try {
                        script.echo "TRYING TO DROP ALL TABLES, USERS AND VIEWS BEFORE RERUNNING LIQUIBASE"

                        if (serviceName == "economicoperator-microservice") {
                            DatabaseActions.dropAllForeignKeysEconomicOperator(resourceGroupName, script)
                            DatabaseActions.dropAllTablesEconomicOperator(resourceGroupName, script)
                            DatabaseActions.dropAllUsersEconomicOperator(resourceGroupName, script)
                            DatabaseActions.dropAllViewsEconomicOperator(resourceGroupName, script)
                            DatabaseActions.dropAllSequencesEconomicOperator(resourceGroupName, script)
                            DatabaseActions.dropAllEconomicOperatorFTSCatalogs(resourceGroupName, script)
                        } else {
                            DatabaseActions.dropAllForeignKeys(serviceName, resourceGroupName, script)
                            DatabaseActions.dropAllTemporalTables(serviceName, resourceGroupName, script)
                            DatabaseActions.dropAllTables(serviceName, resourceGroupName, script)
                            DatabaseActions.dropAllUsers(serviceName, resourceGroupName, script)
                            DatabaseActions.dropAllViews(serviceName, resourceGroupName, script)
                            DatabaseActions.dropAllSequences(serviceName, resourceGroupName, script)
                            DatabaseActions.dropAllFullTextCatalogs(serviceName, resourceGroupName, script)
                        }

                        if (dockerContainerVersion != null) {
                            deployLatestMasterConfiguration(resourceGroupName, serviceName, subscription, dockerImageName, script)
                        } else {
                            script.echo "LATEST MASTER DOES NOT EXIST, CONTINUING TO RUN FEATURE CONFIGURATION"
                        }
                    } catch (e) {
                        script.echo "TRYING TO DROP DATABASE BEFORE RERUNNING LIQUIBASE"
                        DatabaseActions.dropDatabase(serviceName, resourceGroupName, script)
                        DatabaseActions.createDatabase(serviceName, resourceGroupName, script)
                        deployLatestMasterConfiguration(resourceGroupName, serviceName, subscription, dockerImageName, script)
                    }

                    if (dockerImageName == componentDockerImageName) {
                        ranConfig = true
                    }
                }
            }
        }

        if (DeployQueries.hasConfiguration(serviceName, subscription, script) && !ranConfig) {
            deployConfigurationOnly(resourceGroupName, serviceName, subscription, componentDockerImageName, script)
            script.echo "RAN CONFIGURATION"
        }

        AzureActions.deployComponent(resourceGroupName, serviceName, serviceName, subscription, containerRepository, expectedVersion, script)
    }

    /**
     * Run the configuration docker container, assumes there is one
     *
     * @param resourceGroupName         The name of the resource group to release to
     * @param serviceName               The service to deploy
     * @param subscription              The subscription, e.g. Sandpit
     * @param componentDockerImageName  The docker image of the main component
     * @param script                    The global script parameter
     */
    public static void deployConfigurationOnly(String resourceGroupName, String serviceName, String subscription, String componentDockerImageName, Script script) {
        def configDockerImageName = componentDockerImageName.replace(serviceName, serviceName + "-configuration")
        String databaseVars = getDatabaseVariables(resourceGroupName, serviceName, subscription, script)
        String getAzureSearchVariables = getAzureSearchVariables(resourceGroupName, serviceName, subscription, script)
        String blobStorageVars = getBlobStorageVariables(serviceName, "sndimpinfsto003", script)
        String loadDataVar = loadReducedData(resourceGroupName)
        String loadLowRiskCountryForTest = loadLowRiskCountry(resourceGroupName)
        ScriptActions.runCommandLogOnlyOnError("docker pull ${configDockerImageName}", script)
        def dockerRunCommandToRun = """set +x; docker run -t ${databaseVars} ${getAzureSearchVariables} ${blobStorageVars} ${loadDataVar} ${loadLowRiskCountryForTest} --rm ${configDockerImageName}"""

        script.echo "Running configuration for ${serviceName}"
        script.sh(script: dockerRunCommandToRun)
        script.echo "Configuration complete for ${serviceName}"
    }

    /**
     * Attempting to execute deploy of latest master container
     * @param resourceGroupName         The name of the resource group to deploy to
     * @param serviceName               The service to deploy
     * @param subscription              The subscription, e.g. Sandpit
     * @param dockerImageName           The docker image of the master component
     * @param script                    The global script parameter
     */
    private static void deployLatestMasterConfiguration(String resourceGroupName, String serviceName, String subscription, String dockerImageName, Script script) {
        try {
            script.echo "TRYING TO RUN LATEST MASTER CONFIGURATION CONTAINER"
            deployConfigurationOnly(resourceGroupName, serviceName, subscription, dockerImageName, script)
            script.echo "SUCCESSFULLY RESET DATABASE TO LATEST MASTER VERSION"
        } catch (e) {
            script.echo "ERROR RUNNING LATEST MASTER CONFIGURATION, SKIPPING TO FEATURE CONTAINER"
        }
    }

    /**
     * Determine if we should load reduced datasets in static envs
     *
     * @param resourceGroupName     The name of Resource Group we're deploying to
     */
    private static String loadReducedData(String resourceGroupName){
        def loadReducedData = "false"

        def groupNames = ["SNDIMPINFRGP001-Pool",
                          "SNDIMPINFRGP001-imports-static-test",
                          "SNDIMPINFRGP001-imports-static-integration",
                          "SNDIMPINFRGP001-Hotfix-Pool"]

        if (groupNames.any { resourceGroupName.contains(it) }) {
            loadReducedData = "true"
        }

        return """ \
            -e "LOAD_REDUCED_DATA=${loadReducedData}" \
        """
    }

    /**
     * Determine if we should load a low risk country into static and VNET environments for testing
     *
     * @param resourceGroupName     The name of Resource Group we're deploying to
     */
    private static String loadLowRiskCountry(String resourceGroupName){
        def loadLowRiskCountry = "false"
        def groupNames = ["SNDIMPINFRGP001-Pool",
                          "SNDIMPINFRGP001-imports-static-test",
                          "SNDIMPINFRGP001-imports-static-integration",
                          "SNDIMPINFRGP001-imports-static-vnet",
                          "SNDIMPINFRGP001-Hotfix-Pool"]

        if (groupNames.any { resourceGroupName.contains(it) }) {
            loadLowRiskCountry = "true"
        }

        return """ \
            -e "LOAD_LOW_RISK_COUNTRY=${loadLowRiskCountry}" \
        """
    }

    /**
     * Get the details of the blob storage account
     *
     * @param serviceName         The service we're deploying
     * @param blobStorageAccount  The blob storage account we're accessing
     * @param script              The global script parameter
     */
    private static String getBlobStorageVariables(String serviceName, String blobStorageAccount, Script script) {
        def sas = ""
        def (shortName, microserviceString) = serviceName.tokenize("-")
        def storageUrl = "https://${blobStorageAccount}.blob.core.windows.net/${shortName}"
        boolean shouldAddBlobVariables = true
        try {
            sas = VaultKey.getSecuredValue("storage-${blobStorageAccount}-${shortName}-sas", script)
        } catch (Exception e) {
            shouldAddBlobVariables = false
        }
        if (!shouldAddBlobVariables) {
            return ""
        }
        return """ \
            -e "STORAGE_SAS=${sas}" \
            -e "STORAGE_BASE_URL=${storageUrl}" \
        """
    }

    /**
     * Get the details of the database variables
     *
     * @param resourceGroupName  The name of the resource group to release to
     * @param serviceName        The service we're deploying
     * @param subscription       The subscription, e.g. Sandpit
     * @param script             The global script parameter
     */
    private static String getDatabaseVariables(String resourceGroupName, String serviceName, String subscription, Script script) {

        if (serviceName == "economicoperator-microservice") {
            return getDatabaseVariablesEconomicOperator(resourceGroupName, serviceName, subscription, script)
        }

        if (serviceName == "legacy-notifications-microservice") {
            return ""
        }

        if (serviceName.startsWith("integration-platform-database")) {
            return getDatabaseVariablesIntegrationPlatformDatabase(serviceName, script)
        }

        def parametersMap = Config.getParameters(serviceName, subscription.toLowerCase(), script)

        def databaseAdminUsername = Config.getPropertyValue("serviceAdminDBUsername", script)
        def databaseAdminPassword = VaultKey.getSecuredValue("DatabaseDBPassword", script)

        def serviceDBUsername
        def serviceDBPassword
        def serviceDBPasswordNew
        def recreateAzureSearchIndex

        recreateAzureSearchIndex = parametersMap.parameters.recreateAzureSearchIndex?.value ?: "false"
        serviceDBUsername = parametersMap.parameters.serviceDBUsername ? parametersMap.parameters.serviceDBUsername.value : ""
        serviceDBPassword = VaultKey.getSecuredValue("${serviceName}DatabasePassword", script)
        serviceDBPasswordNew = VaultKey.getSecuredValue("${serviceName}DatabasePassword-snd", script)

        def serviceDBUsernameAD = parametersMap.parameters.serviceDBUsernameAD.value
        def dbName = PoolTag.getNameWithTag(serviceName, resourceGroupName)

        def databaseConnection = "jdbc:sqlserver://${parametersMap.parameters.serviceDBHost.value}:" +
                "1433;databaseName=${dbName}" +
                ";encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;" +
                "loginTimeout=30;authentication=ActiveDirectoryPassword"

        def databaseVariables = """ \
        -e "DATABASE_DB_CONNECTION_STRING=${databaseConnection}" \
        -e "DATABASE_DB_USER=${databaseAdminUsername}" \
        -e "DATABASE_DB_PASSWORD=${databaseAdminPassword}" \
        -e "BASE_SERVICE_DB_USER=${serviceDBUsername}" \
        -e "BASE_SERVICE_DB_PASSWORD=${serviceDBPassword}" \
        -e "NEW_BASE_SERVICE_DB_PASSWORD=${serviceDBPasswordNew}" \
        -e "BASE_SERVICE_DB_USER_AD=${serviceDBUsernameAD}" \
        -e "DATABASE_DB_NAME=${dbName}" \
        -e "DATABASE_DB_HOST=${parametersMap.parameters.serviceDBHost.value}" \
        -e "DATABASE_DB_PORT=1433" \
        -e "RECREATE_AZURE_SEARCH_INDEX=${recreateAzureSearchIndex}" \
        -e "env=snd" \
        """

        return databaseVariables
    }

    /**
     * Get the details of the economic operator database variables
     * @param resourceGroupName  The name of the resource group to release to
     * @param serviceName        The service we're deploying
     * @param subscription       The subscription, e.g. Sandpit
     * @param script             The global script parameter
     * @return
     */
    private static String getDatabaseVariablesEconomicOperator(String resourceGroupName, String serviceName, String subscription, Script script) {
        def parametersMap = Config.getParameters(serviceName, subscription.toLowerCase(), script)

        def recreateAzureSearchIndex = parametersMap.parameters.recreateAzureSearchIndex?.value ?: "false"
        def multipleDatasourceEnabled = parametersMap.parameters.multipleDatasourceEnabled.value
        def databaseAdminUsername = Config.getPropertyValue("serviceAdminDBUsername", script)
        def databaseAdminPassword = VaultKey.getSecuredValue("DatabaseDBPassword", script)
        def serviceDBUsername = parametersMap.parameters.serviceDBUsername.value
        def serviceDBUsernameAD = parametersMap.parameters.serviceDBUsernameAD.value
        def serviceDBPasswordNew = VaultKey.getSecuredValue("${serviceName}DatabasePassword-snd", script)
        def privateDbName = PoolTag.getNameWithTag("economicoperator-microservice", resourceGroupName)
        def publicDbName = PoolTag.getNameWithTag("economicoperator-microservice-public", resourceGroupName)

        def databaseConnectionPrivate = "jdbc:sqlserver://${parametersMap.parameters.serviceDBHost.value}:" +
                "1433;databaseName=${privateDbName}" +
                ";encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;" +
                "loginTimeout=30;authentication=ActiveDirectoryPassword"
        def databaseConnectionPublic = "jdbc:sqlserver://${parametersMap.parameters.serviceDBHost.value}:" +
                "1433;databaseName=${publicDbName}" +
                ";encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;" +
                "loginTimeout=30;authentication=ActiveDirectoryPassword"
        def serviceDBPassword = VaultKey.getSecuredValue("${serviceName}DatabasePassword", script)

        def readOnlyDatabase = parametersMap.parameters.readOnlyDatabase.value
        def serviceDBPasswordADSecretName = (readOnlyDatabase) ? "readOnlyDatabasePasswordAD-snd" : "${serviceName}DatabasePasswordAD-snd"
        def serviceDBPasswordAD = VaultKey.getSecuredValue(serviceDBPasswordADSecretName, script)

        def databaseVariables = """ \
        -e "MULTIPLE_DATASOURCE_ENABLED=${multipleDatasourceEnabled}" \
        -e "DATABASE_DB_CONNECTION_STRING=${databaseConnectionPrivate}" \
        -e "PRIVATE_DATABASE_DB_CONNECTION_STRING=${databaseConnectionPrivate}" \
        -e "PUBLIC_DATABASE_DB_CONNECTION_STRING=${databaseConnectionPublic}" \
        -e "DATABASE_DB_USER=${databaseAdminUsername}" \
        -e "DATABASE_DB_PASSWORD=${databaseAdminPassword}" \
        -e "BASE_SERVICE_DB_USER=${serviceDBUsername}" \
        -e "BASE_SERVICE_DB_PASSWORD=${serviceDBPassword}" \
        -e "NEW_BASE_SERVICE_DB_PASSWORD=${serviceDBPasswordNew}" \
        -e "BASE_SERVICE_DB_USER_AD=${serviceDBUsernameAD}" \
        -e "DATABASE_DB_NAME=${privateDbName}" \
        -e "PRIVATE_DATABASE_DB_NAME=${privateDbName}" \
        -e "PUBLIC_DATABASE_DB_NAME=${publicDbName}" \
        -e "DATABASE_DB_HOST=${parametersMap.parameters.serviceDBHost.value}" \
        -e "DATABASE_DB_PORT=1433" \
        -e "RECREATE_AZURE_SEARCH_INDEX=${recreateAzureSearchIndex}" \
        -e "env=snd" \
        """

        return databaseVariables
    }

    /**
     * Get the details of the integration platform database variables
     * @param serviceName        The service we're deploying
     * @param script             The global script parameter
     * @return
     */
    private static String getDatabaseVariablesIntegrationPlatformDatabase(String serviceName, Script script) {

        def databaseAdminUsername = Config.getPropertyValue("serviceAdminDBUsername", script)
        def databaseAdminPassword = VaultKey.getSecuredValue("DatabaseDBPassword", script)
        def databaseServer = Config.getPropertyValue("azureDatabaseServer", script)
        def serviceDBPassword = VaultKey.getSecuredValue("${serviceName}DatabasePassword-snd", script)

        def databaseConnection = "jdbc:sqlserver://${databaseServer}.database.windows.net:" +
                "1433;databaseName=${serviceName}" +
                ";encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;" +
                "loginTimeout=30;authentication=ActiveDirectoryPassword"

        def databaseVariables = """ \
        -e "DATABASE_DB_CONNECTION_STRING=${databaseConnection}" \
        -e "DATABASE_DB_USER=${databaseAdminUsername}" \
        -e "DATABASE_DB_PASSWORD=${databaseAdminPassword}" \
        -e "BASE_SERVICE_DB_PASSWORD=${serviceDBPassword}" \
        """

        return databaseVariables
    }

    /**
     * Get the details of the Azure Search variables
     *
     * @param resourceGroupName  The name of the resource group to release to
     * @param serviceName        The service we're deploying
     * @param subscription       The subscription, e.g. Sandpit
     * @param script             The global script parameter
     */
    private static String getAzureSearchVariables(String resourceGroupName, String serviceName, String subscription, Script script) {

        if (serviceName == "economicoperator-microservice") {
            return getAzureSearchVariablesEconomicOperator(resourceGroupName, serviceName, subscription, script)
        }

        if (serviceName == "legacy-notifications-microservice") {
            return getAzureSearchVariablesLegacyNotification(resourceGroupName, serviceName, subscription, script)
        }

        def azureSearchDbPassword = ""
        try {
            azureSearchDbPassword = VaultKey.getSecuredValue("${serviceName}AzureSearchDatabasePassword", script)
        } catch (e) {
            script.echo("No azure credentials for service, no azure configuration will be passed")
            return ""
        }

        def parametersMap = Config.getParameters(serviceName, subscription.toLowerCase(), script)
        def dbName = PoolTag.getNameWithTag(serviceName, resourceGroupName)
        def resourceGroupId = PoolTag.getId(resourceGroupName)
        def azureKeyForVault = Config.getPropertyValue("azureSearchAdminApiKey", script)
        def azureSearchAdminApiKey = VaultKey.getSecuredValue(azureKeyForVault, script)
        def azureSearchDBUsername = Config.getPropertyValue("${serviceName}AzureSearchDBUsername", script)
        def azureSearchHost = Config.getPropertyValue("azureSearchServiceHost", script)
        def azureSearchDatabaseConnection = "Server=tcp:${parametersMap.parameters.serviceDBHost.value},1433;" +
                "Initial Catalog=${dbName};Persist Security Info=False;User ID=${azureSearchDBUsername};" +
                "Password=${azureSearchDbPassword};MultipleActiveResultSets=False;Encrypt=True;" +
                "TrustServerCertificate=False;Connection Timeout=30;"
        def azureSearchApiVersion = Config.getPropertyValue("azureSearchApiVersion", script)

        return """ \
        -e "DEPLOY_ENVIRONMENT=${resourceGroupId}" \
        -e "AZURE_SEARCH_DB_PASSWORD=${azureSearchDbPassword}" \
        -e "AZURE_SEARCH_ADMIN_API_KEY=${azureSearchAdminApiKey}" \
        -e "AZURE_SEARCH_HOST=${azureSearchHost}" \
        -e "AZURE_SEARCH_DB_CONNECTION_STRING=${azureSearchDatabaseConnection}" \
        -e "AZURE_SEARCH_API_VERSION=${azureSearchApiVersion}" \
        """
    }

    /**
     * Get the details of the Azure Search variables
     *
     * @param resourceGroupName  The name of the resource group to release to
     * @param serviceName        The service we're deploying
     * @param subscription       The subscription, e.g. Sandpit
     * @param script             The global script parameter
     */
    private static String getAzureSearchVariablesEconomicOperator(String resourceGroupName, String serviceName, String subscription, Script script) {
        def parametersMap = Config.getParameters(serviceName, subscription.toLowerCase(), script)

        def azureSearchDbPassword = ""
        try {
            azureSearchDbPassword = VaultKey.getSecuredValue("${serviceName}AzureSearchDatabasePassword", script)
        } catch (e) {
            script.echo("No azure credentials for service, no azure configuration will be passed")
            return ""
        }

        def privateDbName = PoolTag.getNameWithTag("economicoperator-microservice", resourceGroupName)
        def publicDbName = PoolTag.getNameWithTag("economicoperator-microservice-public", resourceGroupName)
        def resourceGroupId = PoolTag.getId(resourceGroupName)
        def azureKeyForVault = Config.getPropertyValue("azureSearchAdminApiKey", script)
        def azureSearchAdminApiKey = VaultKey.getSecuredValue(azureKeyForVault, script)
        def azureSearchDBUsername = Config.getPropertyValue("${serviceName}AzureSearchDBUsername", script)
        def azureSearchHost = Config.getPropertyValue("azureSearchServiceHost", script)
        def azureSearchDatabaseConnectionPrivate = "Server=tcp:${parametersMap.parameters.serviceDBHost.value},1433;" +
                "Initial Catalog=${privateDbName};Persist Security Info=False;User ID=${azureSearchDBUsername};" +
                "Password=${azureSearchDbPassword};MultipleActiveResultSets=False;Encrypt=True;" +
                "TrustServerCertificate=False;Connection Timeout=30;"
        def azureSearchDatabaseConnectionPublic = "Server=tcp:${parametersMap.parameters.serviceDBHost.value},1433;" +
                "Initial Catalog=${publicDbName};Persist Security Info=False;User ID=${azureSearchDBUsername};" +
                "Password=${azureSearchDbPassword};MultipleActiveResultSets=False;Encrypt=True;" +
                "TrustServerCertificate=False;Connection Timeout=30;"
        def azureSearchApiVersion = Config.getPropertyValue("azureSearchApiVersion", script)

        return """ \
        -e "DEPLOY_ENVIRONMENT=${resourceGroupId}" \
        -e "AZURE_SEARCH_DB_PASSWORD=${azureSearchDbPassword}" \
        -e "AZURE_SEARCH_ADMIN_API_KEY=${azureSearchAdminApiKey}" \
        -e "AZURE_SEARCH_HOST=${azureSearchHost}" \
        -e "AZURE_SEARCH_DB_CONNECTION_STRING=${azureSearchDatabaseConnectionPrivate}" \
        -e "AZURE_SEARCH_PRIVATE_DB_CONNECTION_STRING=${azureSearchDatabaseConnectionPrivate}" \
        -e "AZURE_SEARCH_PUBLIC_DB_CONNECTION_STRING=${azureSearchDatabaseConnectionPublic}" \
        -e "AZURE_SEARCH_API_VERSION=${azureSearchApiVersion}" \
        """
    }

    /**
     * Get the details of the Azure Search variables
     *
     * @param resourceGroupName  The name of the resource group to release to
     * @param serviceName        The service we're deploying
     * @param subscription       The subscription, e.g. Sandpit
     * @param script             The global script parameter
     */
    private static String getAzureSearchVariablesLegacyNotification(String resourceGroupName, String serviceName, String subscription, Script script) {
        def resourceGroupId = PoolTag.getId(resourceGroupName)
        def azureKeyForVault = Config.getPropertyValue("azureSearchAdminApiKey", script)
        def azureSearchAdminApiKey = VaultKey.getSecuredValue(azureKeyForVault, script)
        def azureSearchHost = Config.getPropertyValue("azureSearchServiceHost", script)
        def parametersMap = Config.getParameters(serviceName, subscription.toLowerCase(), script)
        def dbName = PoolTag.getNameWithTag("notification-microservice", resourceGroupName)
        def azureSearchDbUsername = Config.getPropertyValue("notification-microserviceAzureSearchDBUsername", script)
        def azureSearchDbPassword = VaultKey.getSecuredValue("notification-microserviceAzureSearchDatabasePassword", script)
        def azureSearchDatabaseConnection = "Server=tcp:${parametersMap.parameters.serviceDBHost.value},1433;" +
                "Initial Catalog=${dbName};Persist Security Info=False;User ID=${azureSearchDbUsername};" +
                "Password=${azureSearchDbPassword};MultipleActiveResultSets=False;Encrypt=True;" +
                "TrustServerCertificate=False;Connection Timeout=30;"
        def azureSearchApiVersion = Config.getPropertyValue("azureSearchApiVersion", script)

        return """ \
        -e "DEPLOY_ENVIRONMENT=${resourceGroupId}" \
        -e "AZURE_SEARCH_ADMIN_API_KEY=${azureSearchAdminApiKey}" \
        -e "AZURE_SEARCH_HOST=${azureSearchHost}" \
        -e "AZURE_SEARCH_DB_CONNECTION_STRING=${azureSearchDatabaseConnection}" \
        -e "AZURE_SEARCH_API_VERSION=${azureSearchApiVersion}" \
        """
    }

    /**
     * Redeploy any broken services
     *
     * @param brokenServices     The table of broken services
     * @param resourceGroupName  The name of the resource group to release to
     * @param serviceName        The service we're deploying
     * @param environment        The environment, e.g. Sandpit
     * @param branchName         The name of the deployed branch
     * @param serviceVersion     The version of the deployed service
     * @param script             The global script parameter
     */
    public static void redeployBrokenServices(String brokenServices, String resourceGroupName, String serviceName, String environment, String branchName, String serviceVersion, Script script) {
        for (brokenDeployment in brokenServices.split('\n')) {
            if (isTableHeading(brokenDeployment)) {
                continue
            }

            script.echo("Deployment for $brokenDeployment is broken. Attempting to redeploy")

            def scriptToRun = "az deployment group delete --name $brokenDeployment --resource-group $resourceGroupName"
            ScriptActions.runCommandLogOnlyOnError(scriptToRun, script)

            if (getServiceNameFromDeploymentName(resourceGroupName, brokenDeployment) == serviceName) {
                def projectName = Config.getPropertyValue("projectName", script)

                String containerRepository = "${projectName.toLowerCase()}-" + Branches.getBranchPrefix(branchName)
                String dockerNameTag = DockerName.getNameAndTag(branchName, serviceName, environment, serviceVersion, false, script)

                deployServiceWithDockerImage(resourceGroupName, serviceName, environment, containerRepository, dockerNameTag, serviceVersion, script)
            } else {
                brokenDeployment = getServiceNameFromDeploymentName(resourceGroupName, brokenDeployment)
                deployService(resourceGroupName, brokenDeployment, environment, script)
            }
        }
    }

    private static boolean isTableHeading(String tableRow) {
        return tableRow == "" || tableRow == "Name" || tableRow.contains("-----")
    }

    private static String getServiceNameFromDeploymentName(String resourceGroupName, String deploymentName) {
        def poolExtension = PoolTag.getId(resourceGroupName)
        return deploymentName.substring(0, deploymentName.length() - (poolExtension.length() + 1))
    }

}
