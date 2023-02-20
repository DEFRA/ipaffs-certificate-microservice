#!/usr/bin/env groovy
import defra.pipeline.names.PoolTag

/**
 * Start up Selenium Grid
 *
 * @param resourceGroupName The resource group to run against
 * @param buildNumber The build number associated with the Jenkins build
 */
def call(String resourceGroupName, String buildNumber, String dockerNetworkName) {

    def poolTag = "${PoolTag.getId(resourceGroupName)}"
    def buildName = "${poolTag}_${buildNumber}"

    sh(label: "Starting Selenium Grid", script: """
        docker ps
        docker run -d -p 0:4444 --rm --label build=${buildName} --name ${buildName}_selenium-hub --network ${dockerNetworkName} selenium/hub:3.141.59-20210713
        docker run -d --rm -v \$(pwd):\$(pwd) --label build=${buildName} --name ${buildName}_selenium-browser-1 --network ${dockerNetworkName} -e HUB_HOST=${buildName}_selenium-hub selenium/node-chrome:3.141.59-20210713
        docker run -d --rm -v \$(pwd):\$(pwd) --label build=${buildName} --name ${buildName}_selenium-browser-2 --network ${dockerNetworkName} -e HUB_HOST=${buildName}_selenium-hub selenium/node-chrome:3.141.59-20210713
        docker run -d --rm -v \$(pwd):\$(pwd) --label build=${buildName} --name ${buildName}_selenium-browser-3 --network ${dockerNetworkName} -e HUB_HOST=${buildName}_selenium-hub selenium/node-chrome:3.141.59-20210713
        docker run -d --rm -v \$(pwd):\$(pwd) --label build=${buildName} --name ${buildName}_selenium-browser-4 --network ${dockerNetworkName} -e HUB_HOST=${buildName}_selenium-hub selenium/node-chrome:3.141.59-20210713
        docker ps
    """)
}
