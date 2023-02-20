#!/usr/bin/env groovy
import defra.pipeline.names.PoolTag

/**
 * Tear down Selenium Grid
 *
 * @param resourceGroupName The resource group to run against
 * @param buildNumber The build number associated with the Jenkins build
 */
def call(String resourceGroupName, String buildNumber) {

    def poolTag = "${PoolTag.getId(resourceGroupName)}"

    sh(label: "Tearing down Selenium Grid", script: """
                            
        echo "Running containers (pre-tear-down) ..."
        docker ps
        BUILDNAME=${poolTag}_${buildNumber}
        docker stop \$(docker ps -a -q --filter="label=build=\${BUILDNAME}")
        echo "Running containers (post-run) ..."
        docker ps
        
    """)
}