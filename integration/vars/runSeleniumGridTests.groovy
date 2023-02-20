#!/usr/bin/env groovy
import defra.pipeline.names.PoolTag

/**
 * Run the selenium tests
 *
 * @param serviceName The frontend service to run selenium tests against
 * @param resourceGroupName The resource group to run against
 * @param branchName The branch to run tests from
 * @param testProfile The test profile to run, e.g. sanity, regression
 * @param proxyUrlExtension Tests to be run against proxy feature or not
 * @param token The Gitlab token used to check out code
 * @param buildNumber Jenkins build number which tests will run against
 * @param token Auth token for Jenkins access
 * @param useLatestTag Use latest tag or not
 * @param e2e Enabled end to end tests or not
 */
def call(String serviceName, String resourceGroupName, String branchName, String testProfile, String proxyUrlExtension = "", String token = "", String buildNumber = "", boolean useLatestTag = false, boolean e2e = false, String zapProxyUrl) {
    // Get the latest tagged green build instead of latest merged master
    // Build may still be in progress, causing breaking selenium changes
    def latestTag = branchName
    if (branchName == "master" && useLatestTag) {
        latestTag = sh(script: "git ls-remote --tags https://jenkins:${token}@giteux.azure.defra.cloud/imports/${serviceName}.git | sort -t '/' -k 3 -V | tail -n1 | sed 's/.*\\///; s/\\^{}//'", returnStdout: true).trim()
    }

    def poolTag = "${PoolTag.getId(resourceGroupName)}"

    def stringyJson = prepareSeleniumParameters(resourceGroupName, testProfile, e2e, proxyUrlExtension, getDockerPort(poolTag, buildNumber), zapProxyUrl)

    def seleniumScriptToRun = "set +x; ./${serviceName}/integration/scripts/runSeleniumGridTests.sh '${stringyJson}'"

    sh(script: "git clone --branch ${latestTag} https://jenkins:${token}@giteux.azure.defra.cloud/imports/${serviceName}.git")
    sh(script: "chmod 555 ./${serviceName}/integration/scripts/runSeleniumGridTests.sh")

    echo seleniumScriptToRun

    def exitCode = sh(script: seleniumScriptToRun, returnStatus: true)
    sh(script: "exit ${exitCode}")
}

def getDockerPort(String poolTag, String buildNumber) {
    def dockerline =  sh(returnStdout: true, label: "Getting Selenium Hub port number", script: """
    docker ps | grep ${poolTag}_${buildNumber}_selenium-hub
    """)
    def dockerport = dockerline =~  /0.0.0.0:(\d*)->/
    return dockerport[0][1]
}
