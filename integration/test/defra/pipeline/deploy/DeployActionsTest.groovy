package defra.pipeline.deploy

import defra.pipeline.BaseTest
import defra.pipeline.environments.WaitForVersion
import org.junit.After
import org.junit.Before
import org.junit.Test

class DeployActionsTest extends BaseTest {

    @Before
    void setUp() {
        WaitForVersion.metaClass.static.checkVersion = { String serviceName, String resourceGroupName, String subscription, String expectedVersionNumber, Script script -> true}
    }

    @After
    void tidyUp() {
        WaitForVersion.metaClass = null
    }

    @Test
    public void testDeployServiceForVersionWithDBAndConfiguration() {
        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"
        shellCommandsReturn["az keyvault secret show.* --name DatabaseDBPassword.*"] = "abcd1234"
        shellCommandsReturn["az keyvault secret show.* --name commoditycode-microserviceDatabasePassword.*"] = "ABCD123456"
        shellCommandsReturn["az keyvault secret show.* --name commoditycode-microserviceDatabasePasswordAD-snd.*"] = "654321DCBA"
        shellCommandsThrow["az keyvault secret show.* --name commoditycode-microserviceAzureSearchDatabasePassword.*"] = "Error: Can't get secret"
        shellCommandsThrow["az keyvault secret show.* --name storage-sndimpinfsto003-commoditycode-sas.*"] = ""
        DeployActions.deployServiceWithDockerImage("SNDIMPINFRGP001-Pool-3", "commoditycode-microservice", "Sandpit", "imports-imta-111", "commoditycode-microservice-test-container", "1.0.0", this)

        def deployServiceCommandsRan = testCommandRan ".*az deployment group create.*"
        assert deployServiceCommandsRan.size() == 1
        assert deployServiceCommandsRan[0].contains("-g SNDIMPINFRGP001-Pool-3")
        assert deployServiceCommandsRan[0].contains("--name commoditycode-microservice-3")
        assert deployServiceCommandsRan[0].contains("--template-file configuration/imports/web_app_services/templates/commoditycode-microservice.json")
        assert deployServiceCommandsRan[0].contains("--parameters configuration/imports/web_app_services/parameters/sandpit/commoditycode-microservice.parameters.json")
        assert deployServiceCommandsRan[0].contains("containerRepository=imports-imta-111")
        assert deployServiceCommandsRan[0].contains("version=1.0.0")

        def dbCreateCommandsRan = testCommandRan "az sql db create.*"
        assert dbCreateCommandsRan.size() == 1

        def configDockerCommandsRan = testCommandRan "set \\+x; docker run .* commoditycode-microservice-configuration-test-container"
        assert configDockerCommandsRan.size() == 1
        assert configDockerCommandsRan[0].contains("DATABASE_DB_CONNECTION_STRING=jdbc:sqlserver://")
        assert configDockerCommandsRan[0].contains(";encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;" +
            "loginTimeout=30;authentication=ActiveDirectoryPassword")
        assert configDockerCommandsRan[0].contains("DATABASE_DB_USER=SA-DEFRA-AZURE-IMP-SQL-SND@Defra.onmicrosoft.com")
        assert configDockerCommandsRan[0].contains("DATABASE_DB_PASSWORD=abcd1234")
        assert configDockerCommandsRan[0].contains("BASE_SERVICE_DB_USER=commodityCodeServiceUser")
        assert configDockerCommandsRan[0].contains("BASE_SERVICE_DB_PASSWORD=ABCD123456")
        assert configDockerCommandsRan[0].contains("BASE_SERVICE_DB_USER_AD=SA-AZURE-IMP-SQL-COMCO-SND@Defra.onmicrosoft.com")
        assert configDockerCommandsRan[0].contains("DATABASE_DB_NAME=")
        assert configDockerCommandsRan[0].contains("DATABASE_DB_HOST=")
        assert configDockerCommandsRan[0].contains("DATABASE_DB_PORT=")
        assert !configDockerCommandsRan[0].contains("AZURE_SEARCH_DB_PASSWORD=")
        assert !configDockerCommandsRan[0].contains("STORAGE_SAS=")
        assert configDockerCommandsRan[0].contains("RECREATE_AZURE_SEARCH_INDEX=false")
    }

    @Test
    public void testDeployServiceForVersionWithDBAndConfigurationNotificationService() {
        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"
        shellCommandsReturn["az keyvault secret show.* --name DatabaseDBPassword.*"] = "abcd1234"
        shellCommandsReturn["az keyvault secret show.* --name notification-microserviceDatabasePassword.*"] = "ABCD123456"
        shellCommandsReturn["az keyvault secret show.* --name notification-microserviceDatabasePasswordAD-snd.*"] = "654321DCBA"
        shellCommandsReturn["az keyvault secret show.* --name storage-sndimpinfsto003-notification-sas.*"] = "SasABCD123456"
        shellCommandsReturn[".*curl .*-X DELETE.*https://imports-azure-search-shared-s2.search.windows.net/.*\\?api-version=2019-05-06"] = "204"
        shellCommandsReturn["az keyvault secret show.* --name notification-microserviceAzureSearchDatabasePassword.*"] = "AzureABCD123456"
        shellCommandsReturn[".*curl .*-d ''.*https://.*.search.windows.net/indexers/\\d+-notifications-indexer/run\\?api-version=2019-05-06"] = "202"
        DeployActions.deployServiceWithDockerImage("SNDIMPINFRGP001-Pool-3", "notification-microservice", "Sandpit", "imports-imta-111", "notification-microservice-test-container", "1.0.0", this)

        def deployServiceCommandsRan = testCommandRan ".*az deployment group create.*"
        assert deployServiceCommandsRan.size() == 1
        assert deployServiceCommandsRan[0].contains("-g SNDIMPINFRGP001-Pool-3")
        assert deployServiceCommandsRan[0].contains("--name notification-microservice-3")
        assert deployServiceCommandsRan[0].contains("--template-file configuration/imports/web_app_services/templates/notification-microservice.json")
        assert deployServiceCommandsRan[0].contains("--parameters configuration/imports/web_app_services/parameters/sandpit/notification-microservice.parameters.json")
        assert deployServiceCommandsRan[0].contains("containerRepository=imports-imta-111")
        assert deployServiceCommandsRan[0].contains("version=1.0.0")

        def dbCreateCommandsRan = testCommandRan "az sql db create.*"
        assert dbCreateCommandsRan.size() == 1

        def configDockerCommandsRan = testCommandRan "set \\+x; docker run .* notification-microservice-configuration-test-container"
        assert configDockerCommandsRan.size() == 1
        assert configDockerCommandsRan[0].contains("DATABASE_DB_CONNECTION_STRING=jdbc:sqlserver://")
        assert configDockerCommandsRan[0].contains(";encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;" +
                "loginTimeout=30;authentication=ActiveDirectoryPassword")
        assert configDockerCommandsRan[0].contains("DATABASE_DB_USER=SA-DEFRA-AZURE-IMP-SQL-SND@Defra.onmicrosoft.com")
        assert configDockerCommandsRan[0].contains("DATABASE_DB_PASSWORD=abcd1234")
        assert configDockerCommandsRan[0].contains("BASE_SERVICE_DB_USER=notificationServiceUser")
        assert configDockerCommandsRan[0].contains("BASE_SERVICE_DB_PASSWORD=ABCD123456")
        assert configDockerCommandsRan[0].contains("BASE_SERVICE_DB_USER_AD=SA-AZURE-IMP-SQL-NOTIF-SND@Defra.onmicrosoft.com")
        assert configDockerCommandsRan[0].contains("DATABASE_DB_NAME=")
        assert configDockerCommandsRan[0].contains("DATABASE_DB_HOST=")
        assert configDockerCommandsRan[0].contains("DATABASE_DB_PORT=")
        assert configDockerCommandsRan[0].contains("AZURE_SEARCH_DB_PASSWORD=AzureABCD123456")
        assert configDockerCommandsRan[0].contains("STORAGE_SAS=SasABCD123456")
        assert configDockerCommandsRan[0].contains("RECREATE_AZURE_SEARCH_INDEX=true")
    }

    @Test
    public void testDeployServiceForVersionWithDBAndConfigurationWithAzureSearchDetails() {
        shellCommandsReturn["az keyvault secret show.* --name DatabaseDBPassword.*"] = "abcd1234"
        shellCommandsReturn["az keyvault secret show.* --name notification-microserviceDatabasePassword.*"] = "ABCD123456"
        shellCommandsReturn["az keyvault secret show.* --name notification-microserviceAzureSearchDatabasePassword.*"] = "AzureABCD123456"
        shellCommandsReturn[".*curl .*-d ''.*https://.*.search.windows.net/indexers/\\d+-notifications-indexer/run\\?api-version=2019-05-06"] = "202"
        DeployActions.deployServiceWithDockerImage("SNDIMPINFRGP001-Pool-12", "notification-microservice", "Sandpit", "imports-imta-111", "notification-microservice-test-container", "1.0.0", this)

        def configDockerCommandsRan = testCommandRan "set \\+x; docker run .* notification-microservice-configuration-test-container"
        assert configDockerCommandsRan.size() == 1
        assert configDockerCommandsRan[0].contains("AZURE_SEARCH_DB_PASSWORD=AzureABCD123456")
        def curlSearchCommandsRan = testCommandRan "set \\+x; curl.*https://imports-azure-search-shared-s2.search.windows.net.*"
    }

    @Test
    public void testDeployServiceForVersionWithDBAndConfigurationWithSasStorage() {
        shellCommandsReturn["az keyvault secret show.* --name DatabaseDBPassword.*"] = "abcd1234"
        shellCommandsReturn["az keyvault secret show.* --name notification-microserviceDatabasePassword.*"] = "ABCD123456"
        shellCommandsReturn["az keyvault secret show.* --name storage-sndimpinfsto003-notification-sas.*"] = "SasABCD123456"
        shellCommandsReturn[".*curl .*-X DELETE.*https://imports-azure-search-shared-s2.search.windows.net/.*\\?api-version=2019-05-06"] = "204"
        shellCommandsReturn[".*curl .*-d ''.*https://.*.search.windows.net/indexers/\\d+-notifications-indexer/run\\?api-version=2019-05-06"] = "202"
        DeployActions.deployServiceWithDockerImage("SNDIMPINFRGP001-Pool-12", "notification-microservice", "Sandpit", "imports-imta-111", "notification-microservice-test-container", "1.0.0", this)

        def configDockerCommandsRan = testCommandRan "set \\+x; docker run .* notification-microservice-configuration-test-container"
        assert configDockerCommandsRan.size() == 1
        assert configDockerCommandsRan[0].contains("STORAGE_SAS=SasABCD123456")
    }

    @Test
    public void testDeployServiceForVersionWithoutDBAndConfiguration() {
        DeployActions.deployServiceWithDockerImage("SNDIMPINFRGP001-Pool-12", "frontend-notification", "Sandpit", "imports-imta-111", "frontend-notification-test-container", "1.0.0", this)

        def deployServiceCommandsRan = testCommandRan ".*az deployment group create.*"
        assert deployServiceCommandsRan.size() == 1
        assert deployServiceCommandsRan[0].contains("-g SNDIMPINFRGP001-Pool-12")
        assert deployServiceCommandsRan[0].contains("--name frontend-notification-12")
        assert deployServiceCommandsRan[0].contains("--template-file configuration/imports/web_app_services/templates/frontend-notification.json")
        assert deployServiceCommandsRan[0].contains("--parameters configuration/imports/web_app_services/parameters/sandpit/frontend-notification.parameters.json")
        assert deployServiceCommandsRan[0].contains("containerRepository=imports-imta-111")
        assert deployServiceCommandsRan[0].contains("version=1.0.0")

        def dbCreateCommandsRan = testCommandRan "az sql db create.*"
        assert dbCreateCommandsRan.size() == 0

        def configDockerCommandsRan = testCommandRan ".*docker run .* notification-microservice-configuration-test-container"
        assert configDockerCommandsRan.size() == 0
    }

    @Test
    public void testDeployServiceForVersionWithDBAndConfigurationAndOnPool() {
        shellCommandsReturn["az keyvault secret show.* --name DatabaseDBPassword.*"] = "abcd1234"
        shellCommandsReturn["az keyvault secret show.* --name notification-microserviceDatabasePassword.*"] = "ABCD123456"
        shellCommandsReturn["az keyvault secret show.* --name storage-sndimpinfsto003-notification-sas.*"] = "SasABCD123456"
        shellCommandsReturn[".*curl .*-X DELETE.*https://imports-azure-search-shared-s2.search.windows.net/.*\\?api-version=2019-05-06"] = "204"
        shellCommandsReturn[".*curl .*-d ''.*https://.*.search.windows.net/indexers/\\d+-notifications-indexer/run\\?api-version=2019-05-06"] = "202"
        DeployActions.deployServiceWithDockerImage("SNDIMPINFRGP001-Pool-12", "notification-microservice", "Sandpit", "imports-imta-111", "notification-microservice-test-container", "1.0.0", this)

        def configDockerCommandsRan = testCommandRan "set \\+x; docker run .* notification-microservice-configuration-test-container"
        assert configDockerCommandsRan.size() == 1
        assert configDockerCommandsRan[0].contains("LOAD_REDUCED_DATA=true")
        assert configDockerCommandsRan[0].contains("LOAD_LOW_RISK_COUNTRY=true")
    }

    @Test
    public void testDeployServiceForVersionWithDBAndConfigurationAndOnStaticTest() {
        shellCommandsReturn["az keyvault secret show.* --name DatabaseDBPassword.*"] = "abcd1234"
        shellCommandsReturn["az keyvault secret show.* --name notification-microserviceDatabasePassword.*"] = "ABCD123456"
        shellCommandsReturn["az keyvault secret show.* --name storage-sndimpinfsto003-notification-sas.*"] = "SasABCD123456"
        shellCommandsReturn[".*curl .*-X DELETE.*https://imports-azure-search-shared-s2.search.windows.net/.*\\?api-version=2019-05-06"] = "204"
        shellCommandsReturn[".*curl .*-d ''.*https://.*.search.windows.net/indexers/\\d+-notifications-indexer/run\\?api-version=2019-05-06"] = "202"
        DeployActions.deployServiceWithDockerImage("SNDIMPINFRGP001-imports-static-test", "notification-microservice", "Sandpit", "imports-imta-111", "notification-microservice-test-container", "1.0.0", this)

        def configDockerCommandsRan = testCommandRan "set \\+x; docker run .* notification-microservice-configuration-test-container"
        assert configDockerCommandsRan.size() == 1
        assert configDockerCommandsRan[0].contains("LOAD_REDUCED_DATA=true")
        assert configDockerCommandsRan[0].contains("LOAD_LOW_RISK_COUNTRY=true")
    }

    @Test
    public void testDeployServiceForVersionWithDBAndConfigurationAndOnIntegration() {
        shellCommandsReturn["az keyvault secret show.* --name DatabaseDBPassword.*"] = "abcd1234"
        shellCommandsReturn["az keyvault secret show.* --name notification-microserviceDatabasePassword.*"] = "ABCD123456"
        shellCommandsReturn["az keyvault secret show.* --name storage-sndimpinfsto003-notification-sas.*"] = "SasABCD123456"
        shellCommandsReturn[".*curl .*-X DELETE.*https://imports-azure-search-shared-s2.search.windows.net/.*\\?api-version=2019-05-06"] = "204"
        shellCommandsReturn[".*curl .*-d ''.*https://.*.search.windows.net/indexers/\\d+-notifications-indexer/run\\?api-version=2019-05-06"] = "202"
        DeployActions.deployServiceWithDockerImage("SNDIMPINFRGP001-imports-static-integration", "notification-microservice", "Sandpit", "imports-imta-111", "notification-microservice-test-container", "1.0.0", this)

        def configDockerCommandsRan = testCommandRan "set \\+x; docker run .* notification-microservice-configuration-test-container"
        assert configDockerCommandsRan.size() == 1
        assert configDockerCommandsRan[0].contains("LOAD_REDUCED_DATA=true")
        assert configDockerCommandsRan[0].contains("LOAD_LOW_RISK_COUNTRY=true")
    }

    @Test
    public void testDeployServiceForVersionWithDBAndConfigurationAndOnHotfix() {
        WaitForVersion.metaClass.static.checkVersion = { String serviceName, String resourceGroupName, String subscription, String expectedVersionNumber, Script script -> true}

        shellCommandsReturn["az keyvault secret show.* --name DatabaseDBPassword.*"] = "abcd1234"
        shellCommandsReturn["az keyvault secret show.* --name notification-microserviceDatabasePassword.*"] = "ABCD123456"
        shellCommandsReturn["az keyvault secret show.* --name storage-sndimpinfsto003-notification-sas.*"] = "SasABCD123456"
        shellCommandsReturn[".*curl .*-X DELETE.*https://imports-azure-search-shared-s2.search.windows.net/.*\\?api-version=2019-05-06"] = "204"
        shellCommandsReturn[".*curl .*-d ''.*https://.*.search.windows.net/indexers/\\d+-notifications-indexer/run\\?api-version=2019-05-06"] = "202"
        DeployActions.deployServiceWithDockerImage("SNDIMPINFRGP001-Hotfix-Pool-990", "notification-microservice", "Sandpit", "imports-imta-111", "notification-microservice-test-container", "1.0.0", this)

        def configDockerCommandsRan = testCommandRan "set \\+x; docker run .* notification-microservice-configuration-test-container"
        assert configDockerCommandsRan.size() == 1
        assert configDockerCommandsRan[0].contains("LOAD_REDUCED_DATA=true")
        assert configDockerCommandsRan[0].contains("LOAD_LOW_RISK_COUNTRY=true")

        WaitForVersion.metaClass = null
    }

    @Test
    public void testDeployServiceForVersionWithDBAndConfigurationAndOnVnet() {
        shellCommandsReturn["az keyvault secret show.* --name DatabaseDBPassword.*"] = "abcd1234"
        shellCommandsReturn["az keyvault secret show.* --name notification-microserviceDatabasePassword.*"] = "ABCD123456"
        shellCommandsReturn["az keyvault secret show.* --name storage-sndimpinfsto003-notification-sas.*"] = "SasABCD123456"
        shellCommandsReturn[".*curl .*-X DELETE.*https://imports-azure-search-shared-s2.search.windows.net/.*\\?api-version=2019-05-06"] = "204"
        shellCommandsReturn[".*curl .*-d ''.*https://.*.search.windows.net/indexers/\\d+-notifications-indexer/run\\?api-version=2019-05-06"] = "202"
        DeployActions.deployServiceWithDockerImage("SNDIMPINFRGP001-imports-static-vnet", "notification-microservice", "Sandpit", "imports-imta-111", "notification-microservice-test-container", "1.0.0", this)

        def configDockerCommandsRan = testCommandRan "set \\+x; docker run .* notification-microservice-configuration-test-container"
        assert configDockerCommandsRan.size() == 1
        assert configDockerCommandsRan[0].contains("LOAD_REDUCED_DATA=false")
        assert configDockerCommandsRan[0].contains("LOAD_LOW_RISK_COUNTRY=true")
    }

    @Test
    public void testDeployServiceForVersionWithDBAndConfigurationAndOnSnd() {
        shellCommandsReturn["az keyvault secret show.* --name DatabaseDBPassword.*"] = "abcd1234"
        shellCommandsReturn["az keyvault secret show.* --name notification-microserviceDatabasePassword.*"] = "ABCD123456"
        shellCommandsReturn["az keyvault secret show.* --name storage-sndimpinfsto003-notification-sas.*"] = "SasABCD123456"
        shellCommandsReturn[".*curl .*-X DELETE.*https://imports-azure-search-shared-s2.search.windows.net/.*\\?api-version=2019-05-06"] = "204"
        shellCommandsReturn[".*curl .*-d ''.*https://.*.search.windows.net/indexers/\\d+-notifications-indexer/run\\?api-version=2019-05-06"] = "202"
        DeployActions.deployServiceWithDockerImage("SNDIMPINFRGP009", "notification-microservice", "Sandpit", "imports-imta-111", "notification-microservice-test-container", "1.0.0", this)

        def configDockerCommandsRan = testCommandRan "set \\+x; docker run .* notification-microservice-configuration-test-container"
        assert configDockerCommandsRan.size() == 1
        assert configDockerCommandsRan[0].contains("LOAD_REDUCED_DATA=false")
        assert configDockerCommandsRan[0].contains("LOAD_LOW_RISK_COUNTRY=false")
    }

    @Test
    public void testDeployServiceForVersionWithDBAndConfigurationAndOnTst() {
        shellCommandsReturn["az keyvault secret show.* --name DatabaseDBPassword.*"] = "abcd1234"
        shellCommandsReturn["az keyvault secret show.* --name notification-microserviceDatabasePassword.*"] = "ABCD123456"
        shellCommandsReturn["az keyvault secret show.* --name storage-sndimpinfsto003-notification-sas.*"] = "SasABCD123456"
        shellCommandsReturn[".*curl .*-X DELETE.*https://imports-azure-search-shared-s2.search.windows.net/.*\\?api-version=2019-05-06"] = "204"
        shellCommandsReturn[".*curl .*-d ''.*https://.*.search.windows.net/indexers/\\d+-notifications-indexer/run\\?api-version=2019-05-06"] = "202"
        DeployActions.deployServiceWithDockerImage("TSTIMPINFRGP001", "notification-microservice", "Sandpit", "imports-imta-111", "notification-microservice-test-container", "1.0.0", this)

        def configDockerCommandsRan = testCommandRan "set \\+x; docker run .* notification-microservice-configuration-test-container"
        assert configDockerCommandsRan.size() == 1
        assert configDockerCommandsRan[0].contains("LOAD_REDUCED_DATA=false")
        assert configDockerCommandsRan[0].contains("LOAD_LOW_RISK_COUNTRY=false")
    }

    @Test
    public void testDeployServiceWithLatestReleaseWhenNeedsUpgrading() {
        shellCommandsReturn["az deployment group list.*SNDIMPINFRGP001-Pool-12.*commoditycode-microservice.*"] = "- sndeuxfesacr001.azurecr.io/imports-release/commoditycode-microservice:1.0.100"
        shellCommandsReturn["az acr repository list.*"] = "imports-master/notification-service\nimports-release/commoditycode-microservice\nimports-staging/commoditycode-microservice\nimports-staging/commoditycode-microservice-configuration"
        shellCommandsReturn["az acr repository show-tags.*"] = "1.0.101"
        shellCommandsThrow["az keyvault secret show.* --name commoditycode-microserviceAzureSearchDatabasePassword.*"] = "Error: Can't get secret"
        shellCommandsThrow["az keyvault secret show.* --name storage-sndimpinfsto003-commoditycode-sas.*"] = ""

        DeployActions.deployService("SNDIMPINFRGP001-Pool-12", "commoditycode-microservice", "Sandpit", this)

        def deployServiceCommandsRan = testCommandRan ".*az deployment group create.*"
        assert deployServiceCommandsRan.size() == 1
        assert deployServiceCommandsRan[0].contains("1.0.101")
    }

    @Test
    public void testDeployServiceWithLatestReleaseWhenNeedsUpgradingFrontend() {
        shellCommandsReturn["az deployment group list.*SNDIMPINFRGP001-Pool-12.*frontend-notification.*"] = "- sndeuxfesacr001.azurecr.io/imports-release/frontend-notification:1.0.100"
        shellCommandsReturn["az acr repository list.*"] = "imports-master/frontend-notification\nimports-release/frontend-notification\nimports-staging/frontend-notification\nimports-staging/frontend-notification-configuration"
        shellCommandsReturn["az acr repository show-tags.*"] = "1.0.101"

        DeployActions.deployService("SNDIMPINFRGP001-Pool-12", "frontend-notification", "Sandpit", this)

        def deployServiceCommandsRan = testCommandRan ".*az deployment group create.*"
        assert deployServiceCommandsRan.size() == 1
        assert deployServiceCommandsRan[0].contains("1.0.101")
    }

    @Test
    public void testDeployServiceWithLatestReleaseWhenNeedsUpgradingFrontendHealthCheck404() {
        shellCommandsReturn["az deployment group list.*SNDIMPINFRGP001-Pool-12.*frontend-notification.*"] = "- sndeuxfesacr001.azurecr.io/imports-release/frontend-notification:1.0.100"
        shellCommandsReturn["az acr repository list.*"] = "imports-master/frontend-notification\nimports-release/frontend-notification\nimports-staging/frontend-notification\nimports-staging/frontend-notification-configuration"
        shellCommandsReturn["az acr repository show-tags.*"] = "1.0.101"
        shellCommandsReturn["curl -sk -m 10 --header \"x-auth-basic : Basic.*\" https://frontend-notification-12.azurewebsites.net/admin/health-check"] = "404"

        DeployActions.deployService("SNDIMPINFRGP001-Pool-12", "frontend-notification", "Sandpit", this)

        def deployServiceCommandsRan = testCommandRan ".*az deployment group create.*"
        assert deployServiceCommandsRan.size() == 1
        assert deployServiceCommandsRan[0].contains("1.0.101")
    }

    @Test
    public void testNoDeployServiceWithLatestReleaseWhenUpToDate() {
        shellCommandsReturn["az deployment group list.*SNDIMPINFRGP001-Pool-12.*commoditycode-microservice.*"] = "- sndeuxfesacr001.azurecr.io/imports-release/commoditycode-microservice:1.0.100"
        shellCommandsReturn["az acr repository list.*"] = "imports-master/notification-service\nimports-release/commoditycode-microservice\nimports-staging/commoditycode-microservice\nimports-staging/commoditycode-microservice-configuration"
        shellCommandsReturn["az acr repository show-tags.*"] = "1.0.100"
        shellCommandsReturn["curl -sk -m 10 --header \"x-auth-basic : Basic.*\" https://commoditycode-microservice-12.azurewebsites.net/admin/health-check"] = "200"
        shellCommandsReturn["curl -sk -m 10 --header \"x-auth-basic : Basic.*\" https://commoditycode-microservice-12.azurewebsites.net/admin/info.*"] = "1.0.100"

        DeployActions.deployService("SNDIMPINFRGP001-Pool-12", "frontend-notification", "Sandpit", this)

        def deployServiceCommandsRan = testCommandRan ".*az deployment group create.*"
        assert deployServiceCommandsRan.size() == 0
    }

    @Test
    public void testDeployServiceWithReleaseVersion() {
        shellCommandsReturn["az deployment group list.*SNDIMPINFRGP001-Pool-12.*frontend-notification.*"] = "- sndeuxfesacr001.azurecr.io/imports-release/frontend-notification:1.0.100"

        DeployActions.deployService("SNDIMPINFRGP001-Pool-12", "frontend-notification", "Sandpit", false, this)

        def deployServiceCommandsRan = testCommandRan ".*az deployment group create.*"
        assert deployServiceCommandsRan.size() == 1
    }

    @Test
    public void testDeployAllComponentsToLatestMaster() {
        shellCommandsReturn["az keyvault secret show.* --name DatabaseDBPassword.*"] = "abcd1234"
        def deployableComponents = [
            "frontend-decision",
            "decision-microservice",
            "economicoperator-microservice",
            "notification-microservice",
            "soaprequest-microservice",
            "soapsearch-microservice",
            "frontend-control",
            "commoditycode-microservice",
            "fieldconfig-microservice",
            "permissions",
            "certificate-microservice",
            "countries-microservice",
            "customer-microservice",
            "approvedestablishment-microservice",
            "bip-microservice",
            "laboratories-microservice",
            "frontend-notification",
            "referencedataloader-microservice"
        ]
        def acrRegs = []
        deployableComponents.each {
            acrRegs.add("imports-release/${it}")
            shellCommandsReturn["az keyvault secret show.* --name ${it}DatabasePassword.*"] = "ABCD123456"
            shellCommandsThrow["az keyvault secret show.* --name ${it}AzureSearchDatabasePassword.*"] = "Error: Can't get secret"
            shellCommandsThrow["az keyvault secret show.* --name storage-sndimpinfsto003-${it.replace("-microservice", "")}-sas.*"] = "Error: Can't get secret"
        }

        shellCommandsReturn["az deployment group list.*SNDIMPINFRGP001-Pool-12.*"] = "- sndeuxfesacr001.azurecr.io/imports-release/notification-microservice:1.0.100"
        shellCommandsReturn["az acr repository list.*"] = acrRegs.join('\n')
        shellCommandsReturn["az acr repository show-tags.*"] = "1.0.101"
        shellCommandsReturn[".*curl .*-X DELETE.*https://imports-azure-search-shared-s2.search.windows.net/.*\\?api-version=2019-05-06"] = "204"
        shellCommandsReturn[".*curl .*-d ''.*https://.*.search.windows.net/indexers/\\d+-[a-z-]*-indexer/run\\?api-version=2019-05-06"] = "202"

        DeployActions.deployAllServices("SNDIMPINFRGP001-Pool-12", "Sandpit", ["notification-microservice", "frontend-notification"], this)

        def deployServiceCommandsRan = testCommandRan ".*az deployment group create.*"
        assert deployServiceCommandsRan.size() - 2 == deployableComponents.size()
    }

    @Test
    public void testDeployParallelComponentForBranchBuildComponent() {
        shellCommandsThrow["az keyvault secret show .*commoditycode-microserviceAzureSearchDatabasePassword.*"] = "Error: Can't get secret"
        shellCommandsThrow["az keyvault secret show.* --name storage-sndimpinfsto003-commoditycode-sas.*"] = "Error: Can't get secret"
        DeployActions.deployParallelComponent("SNDIMPINFRGP001-Pool-12", "Sandpit", "feature/IMTA-9999-test", "0.1", "commoditycode-microservice", "commoditycode-microservice", this)
        def deployServiceCommandsRan = testCommandRan ".*az deployment group create.*"
        assert deployServiceCommandsRan[0].contains("--name commoditycode-microservice-12")
        assert deployServiceCommandsRan[0].contains("--template-file configuration/imports/web_app_services/templates/commoditycode-microservice.json")

        WaitForVersion.metaClass = null
        assert deployServiceCommandsRan[0].contains("--template-file configuration/imports/web_app_services/templates/commoditycode-microservice.json")
    }

    @Test
    public void testDeployParallelComponentForNoneBranchBuildComponent() {
        shellCommandsReturn["az deployment group list.*SNDIMPINFRGP001-Pool-12.*commoditycodew-microservice.*"] = "- sndeuxfesacr001.azurecr.io/imports-release/commoditycode-microservice:1.0.100"
        shellCommandsReturn["az acr repository list.*"] = "imports-master/commoditycode-service\nimports-release/commoditycode-microservice\nimports-staging/commoditycode-microservice\nimports-staging/commoditycode-microservice-configuration"
        shellCommandsThrow["az keyvault secret show .*commoditycode-microserviceAzureSearchDatabasePassword.*"] = "Error: Can't get secret"
        shellCommandsReturn["az acr repository show-tags.*"] = "1.0.101"

        DeployActions.deployParallelComponent("SNDIMPINFRGP001-Pool-12", "Sandpit", "feature/IMTA-9999-test", "0.1", "notification-microservice", "commoditycode-microservice", this)
        def deployServiceCommandsRan = testCommandRan ".*az deployment group create.*"
        assert deployServiceCommandsRan[0].contains("--name commoditycode-microservice-12")
        assert deployServiceCommandsRan[0].contains("--template-file configuration/imports/web_app_services/templates/commoditycode-microservice.json ")
    }

    @Test
    public void testDeployServiceToLatestMasterReleaseSkipServices() {
        DeployActions.deployService("SNDIMPINFRGP001-Pool-12", "commoditycode-microservice", "Sandpit", ["commoditycode-microservice"], this)
        def deployServiceCommandsRan = testCommandRan ".*az deployment group create.*"
        assert deployServiceCommandsRan.size() == 0
    }

    @Test
    public void testRedeployBrokenServicesShouldSkipHeading() {
        DeployActions.redeployBrokenServices("Name\n-----\n\n", "", "", "", "", "", this)
        def deleteCommand = testCommandRan "az deployment group delete.*"
        assert deleteCommand.size() == 0
    }

    @Test
    public void testRedeployBrokenServicesShouldDeployFailed() {
        shellCommandsReturn["az deployment group list.*SNDIMPINFRGP001-Pool-12.*economicoperator.*"] = "- sndeuxfesacr001.azurecr.io/imports-release/economicoperator-microservice:1.0.100"
        shellCommandsReturn["az acr repository list.*"] = "imports-master/economicoperator-service\nimports-release/economicoperator-microservice\nimports-staging/economicoperator-microservice\nimports-staging/economicoperator-configuration"
        shellCommandsReturn["az acr repository show-tags.*"] = "1.0.101"
        shellCommandsThrow["az keyvault secret show .*-microserviceAzureSearchDatabasePassword.*"] = "Error: Can't get secret"
        shellCommandsThrow["az keyvault secret show.* --name storage-sndimpinfsto003-.*-sas.*"] = "Error: Can't get secret"

        DeployActions.redeployBrokenServices("Name\n-----\n\ncommoditycode-microservice-12\neconomicoperator-microservice-12", "SNDIMPINFRGP001-Pool-12", "commoditycode-microservice", "Sandpit", "feature/IMTA-9999-test", "0.1", this)
        def deleteNotificationCommand = testCommandRan "az deployment group delete --name commoditycode-microservice-12 --resource-group SNDIMPINFRGP001-Pool-12"
        assert deleteNotificationCommand.size() == 1

        def deletePermissionsCommand = testCommandRan "az deployment group delete --name economicoperator-microservice-12 --resource-group SNDIMPINFRGP001-Pool-12"
        assert deletePermissionsCommand.size() == 1

        def createNotificationService = testCommandRan "az deployment group create -g SNDIMPINFRGP001-Pool-12 --name commoditycode-microservice.*"
        assert createNotificationService.size() == 1

        def createEOService = testCommandRan "az deployment group create -g SNDIMPINFRGP001-Pool-12 --name economicoperator-microservice.*"
        assert createEOService.size() == 1
    }
}
