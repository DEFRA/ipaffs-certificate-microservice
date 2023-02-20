package defra.pipeline.environment

import defra.pipeline.deploy.DeployQueries
import defra.pipeline.deploy.HealthCheckStatus
import defra.pipeline.BaseTest
import defra.pipeline.environments.WaitForVersion
import org.junit.Test

class WaitForVersionTest extends BaseTest {

    @Test
    public void testHealthCheckWithStatusUP() {
        DeployQueries.metaClass.static.checkHealthCheck = {String serviceName, String resourceGroupName, String subscription, Script script -> HealthCheckStatus.UP}
        DeployQueries.metaClass.static.getCurrentRunningVersion = {String serviceName, String resourceGroupName, String subscription, Script script -> "1.0"}

        assert WaitForVersion.checkVersion("testService", "testResourceGroupName", "testSubscription", "1.0", this)

        DeployQueries.metaClass = null
    }
}
