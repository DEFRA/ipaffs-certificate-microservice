package defra.pipeline.azure

import defra.pipeline.BaseTest

import org.junit.Test

class AzureQueriesTest extends BaseTest {

    @Test
    public void testCachingQueries() {
        shellCommandsReturn["az deployment group list"] = "- sndeuxfesacr001.azurecr.io/imports-release/notification-microservice:1.0.100"
        shellCommandsReturn["md5sum \".*\""] = "abcd1234"

        def out1 = AzureQueries.runCachedQueryForAzCli("az deployment group list", false, this)
        def out2 = AzureQueries.runCachedQueryForAzCli("az deployment group list", false, this)

        assert shellCommandsRan.size() == 1
        assert out1 == "- sndeuxfesacr001.azurecr.io/imports-release/notification-microservice:1.0.100"
        assert out1 == out2
        assert 'azurefiles/42ec0c8a14a374f56254229107c01313' in filesExist
    }

    @Test
    public void testCachingQueriesWithRefresh() {
        shellCommandsReturn["az deployment group list"] = "- sndeuxfesacr001.azurecr.io/imports-release/notification-microservice:1.0.100"
        shellCommandsReturn["md5sum \".*\""] = "abcd1234"

        def out1 = AzureQueries.runCachedQueryForAzCli("az deployment group list", false, this)
        def out2 = AzureQueries.runCachedQueryForAzCli("az deployment group list", true, this)

        assert shellCommandsRan.size() == 2
        assert 'azurefiles/42ec0c8a14a374f56254229107c01313' in filesExist
    }

    @Test
    public void testGetContainerNameOfDeployedService() {
        shellCommandsReturn["az deployment group list.*SNDIMPINFRGP001-Pool-12.*notification-microservice.*"] = "- sndeuxfesacr001.azurecr.io/imports-release/notification-microservice:1.0.100"

        def ret = AzureQueries.getContainerNameOfDeployedService("SNDIMPINFRGP001-Pool-12", "notification-microservice", this)
        assert ret == "sndeuxfesacr001.azurecr.io/imports-release/notification-microservice:1.0.100"

        def checkDeployedCommandsRan = testCommandRan "az deployment group list.*"
        assert checkDeployedCommandsRan.size() == 1
        assert checkDeployedCommandsRan[0].contains("--resource-group SNDIMPINFRGP001-Pool-12")
        assert checkDeployedCommandsRan[0].contains("--query \"[?contains(name, 'notification-microservice')]")
    }

    // TODO: Failed deployment, i.e. provisioningState != "Succeeded"

    @Test
    public void testGetContainerNameOfDeployedServiceWhenNotFound() {
        shellCommandsReturn["az deployment group list.*SNDIMPINFRGP001-Pool-12.*notification-microservice.*"] = ""

        def ret = AzureQueries.getContainerNameOfDeployedService("SNDIMPINFRGP001-Pool-12", "notification-microservice", this)
        assert ret == null
    }

    @Test
    public void testGetLatestMasterReleaseOfService() {
        shellCommandsReturn["az acr repository list.*"] = "imports-master/notification-service\nimports-release/notification-microservice\nimports-staging/notification-microservice\nimports-staging/notification-microservice-configuration"
        shellCommandsReturn["az acr repository show-tags.*"] = "1.0.100"

        def ret = AzureQueries.getServiceVersionForLatestMasterRelease("notification-microservice", this)
        assert ret == "1.0.100"
    }

}
