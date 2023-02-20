package defra.pipeline.deploy

import defra.pipeline.BaseTest
import defra.pipeline.config.Config
import org.junit.Test

class DeployQueriesTest extends BaseTest {

    @Test
    public void testGetListOfDeployableComponents() {
        def ret = DeployQueries.getListOfDeployableComponents("configuration/imports/deploylist/sandpit/deployList.txt", this)
        assert ret.size() > 15
        assert ret.contains("notification-microservice")
        assert ret.contains("frontend-notification")
        assert ret.contains("frontend-decision")
    }

    @Test
    public void testServiceHasDatabase() {
        def ret = DeployQueries.hasDatabase("notification-microservice", "Sandpit", this)
        assert ret == true
    }

    @Test
    public void testServiceHasNoDatabase() {
        def ret = DeployQueries.hasDatabase("frontend-notification", "Sandpit", this)
        assert ret == false
    }

    @Test
    public void testServiceHasConfiguration() {
        def ret = DeployQueries.hasConfiguration("notification-microservice", "Sandpit", this)
        assert ret == true
    }

    @Test
    public void testServiceHasNoConfiguration() {
        def ret = DeployQueries.hasConfiguration("frontend-notification", "Sandpit", this)
        assert ret == false
    }

    @Test
    public void testServiceHasNoReleaseNumberForSandpit() {
        def ret = DeployQueries.getReleaseNumber("sandpit", "frontend-notification", this)
        assert ret == null
    }

    @Test
    public void testServiceHasReleaseNumberForOctopus() {
        def ret = DeployQueries.getReleaseNumber("octopus", "frontend-notification", this)
        assert ret.matches("\\d+\\.\\d+\\.\\d+")
    }

    @Test
    public void testServiceNotInDeployListThatHasConfiguration() {
        this.filesExist.add("configuration/Dockerfile")
        def ret = DeployQueries.hasConfiguration("non-existent-service", "Sandpit", this)
        assert ret == true
    }

    @Test
    public void testServiceNotInDeployListThatHasNoConfiguration() {
        def ret = DeployQueries.hasConfiguration("non-existent-service", "Sandpit", this)
        assert ret == false
    }

    @Test
    public void testGetCurrentRunningServiceVersion() {
        def expectedVersion = "1.0.01"
        shellCommandsReturn["set \\+x; curl -sk -m 10 https://notification-microservice-1.azurewebsites.net/admin/info.*"] = expectedVersion
        def currentRunningServiceVersion = DeployQueries.getCurrentRunningVersion("notification-microservice", "SNDIMPINFRGP001-Pool-1", "Sandpit", this)
        assert currentRunningServiceVersion.equals(expectedVersion)
    }

    @Test
    public void testGetCurrentRunningServiceVersionException() {
        shellCommandsThrow["set \\+x; curl -sk -m 10 https://notification-microservice-1.azurewebsites.net/admin/info.*"] = 'error'
        def currentRunningServiceVersion = DeployQueries.getCurrentRunningVersion("notification-microservice", "SNDIMPINFRGP001-Pool-1", "Sandpit", this)
        assert currentRunningServiceVersion.equals("")
    }

    @Test
    public void testCheckHealthCheck() {
        shellCommandsReturn["set \\+x; curl.*"] = '200'
        def healthCheck = DeployQueries.checkHealthCheck("notification-microservice", "SNDIMPINFRGP001-Pool-1", "Sandpit", this)
        assert healthCheck.equals(HealthCheckStatus.UP)
    }

    @Test
    public void testCheckHealthCheckException() {
        shellCommandsReturn["set \\+x; curl.*"] = '503'
        def healthCheck = DeployQueries.checkHealthCheck("notification-microservice", "SNDIMPINFRGP001-Pool-1", "Sandpit", this)
        assert healthCheck.equals(HealthCheckStatus.DOWN)
    }

    @Test
    public void testCheckHealthCheckNotDeployed() {
        shellCommandsReturn["set \\+x; curl.*"] = '000'
        def healthCheck = DeployQueries.checkHealthCheck("notification-microservice", "SNDIMPINFRGP001-Pool-1", "Sandpit", this)
        assert healthCheck.equals(HealthCheckStatus.NOT_DEPLOYED)
    }

    @Test
    public void testCheckHealthCheckNoEndpoints500() {
        shellCommandsReturn["set \\+x; curl.*"] = '500'
        def healthCheck = DeployQueries.checkHealthCheck("notification-microservice", "SNDIMPINFRGP001-Pool-1", "Sandpit", this)
        assert healthCheck.equals(HealthCheckStatus.ENDPOINTS_FAILED)
    }

    @Test
    public void testCheckHealthCheckNoEndpoints404() {
        shellCommandsReturn["set \\+x; curl.*"] = '404'
        def healthCheck = DeployQueries.checkHealthCheck("notification-microservice", "SNDIMPINFRGP001-Pool-1", "Sandpit", this)
        assert healthCheck.equals(HealthCheckStatus.UNSUPPORTED_NO_ENDPOINTS)
    }

    @Test
    public void testCheckHealthCheckUnrecognised() {shellCommandsReturn["az acr repository list.*"]

        shellCommandsReturn["set \\+x; curl.*"] = '429'
        def healthCheck = DeployQueries.checkHealthCheck("notification-microservice", "SNDIMPINFRGP001-Pool-1", "Sandpit", this)
        assert healthCheck.equals(HealthCheckStatus.UNKNOWN_STATE)
    }

    @Test
    public void testGetCollatedListOfDeployableComponentsSplit() {
        Config.metaClass.static.getPropertyValue = {String name, Script script -> getPropertyValueTestHelper(name, "5")}
        def collate = DeployQueries.getCollatedListOfDeployableComponents("sandpit", this)
        assert collate.size() == 9
        assert collate[0].size() == 5
        assert collate[1].size() == 5
        assert collate[2].size() == 5
        assert collate[3].size() == 5
        assert collate[4].size() == 5

        // Reset metaClass to null to reset the mock
        Config.metaClass = null
    }

    @Test
    public void testGetCollatedListOfDeployableComponentsLargerValueThanList() {
        Config.metaClass.static.getPropertyValue = {String name, Script script -> getPropertyValueTestHelper(name, "100")}
        def collate = DeployQueries.getCollatedListOfDeployableComponents("sandpit", this)
        assert collate.size() == 1
        assert collate[0].size() == 44

        Config.metaClass = null
    }

    @Test
    public void testGetBrokenDeployedComponentsShouldGetBrokenDeployments() {
        def returnedTable = "Name\n-----------\nnotification-microservice-10"
        shellCommandsReturn["az deployment group list --resource-group SNDIMPINFRGP001-Pool-12 --query.*"] = returnedTable
        def brokenDeployments = DeployQueries.getBrokenDeployedComponents("SNDIMPINFRGP001-Pool-12", this)
        assert brokenDeployments.split('\n').size() == 3
        assert brokenDeployments.contains("notification-microservice-10")
    }

    private static String getPropertyValueTestHelper(String name, String maxNumberOfParallelDeployments) {
        if (name == "maxNumberOfParallelDeployments") {
            return maxNumberOfParallelDeployments
        } else if (name == "sandpitDeploymentList") {
            return "configuration/imports/deploylist/sandpit/deployList.txt"
        }  else if (name == "octopusDeploymentList") {
            return "configuration/imports/deploylist/octopus/deployList.txt"
        }
    }
}
