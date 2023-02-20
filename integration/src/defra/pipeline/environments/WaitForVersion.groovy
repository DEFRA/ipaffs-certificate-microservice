package defra.pipeline.environments

import defra.pipeline.deploy.DeployQueries
import defra.pipeline.deploy.HealthCheckStatus

class WaitForVersion {

    static Boolean checkVersion(String serviceName, String resourceGroupName, String subscription, String expectedVersionNumber, Script script) {
        def serviceUp = false
        def maxAttempts = 150
        def attemptNumber = 0

        while (!serviceUp && attemptNumber < maxAttempts) {
            attemptNumber++
            def currentHealthCheckStatus
            while (currentHealthCheckStatus != HealthCheckStatus.UP 
              && currentHealthCheckStatus != HealthCheckStatus.UNSUPPORTED_NO_ENDPOINTS)
            {
              currentHealthCheckStatus = DeployQueries.checkHealthCheck(serviceName, resourceGroupName, subscription, script)
              script.sleep(5)
            }

            def deployedVersion = DeployQueries.getCurrentRunningVersion(serviceName, resourceGroupName, subscription, script)

            script.echo "Deployed Version: ${deployedVersion}"
            script.echo "Expected Version: ${expectedVersionNumber}"
            if (deployedVersion == expectedVersionNumber || currentHealthCheckStatus == HealthCheckStatus.UNSUPPORTED_NO_ENDPOINTS) {
                serviceUp = true
            } else {
                script.echo "Expected version does not match deployed version, sleeping for 5 seconds ..."
                script.sleep(5)
            }
        }
        return serviceUp
    }
}

