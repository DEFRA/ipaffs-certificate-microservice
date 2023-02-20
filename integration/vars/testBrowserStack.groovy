#!/usr/bin/env groovy

/**
 * Run the browser stack tests
 *
 * @param serviceName The frontend service to run selenium tests against
 * @param resourceGroupName The resource group to run against
 * @param branchName The branch to run tests from
 * @param browser The browser to run tests in
 * @param testProfile The test profile to run, e.g. sanity, regression
 */

def call(String serviceName, String resourceGroupName, String branchName, String browser, String testProfile) {
    withCredentials([string(credentialsId: 'JENKINS_GITLAB_TOKEN', variable: 'TOKEN')]) {
        def e2eFlag = false
        def proxyUrlExtension = ""
        def gridPort = ""

        def stringyJson = prepareSeleniumParameters(resourceGroupName, testProfile, e2eFlag, proxyUrlExtension, gridPort, "")
        def seleniumScriptToRun = "set +x; ./${serviceName}/integration/scripts/BrowserStackTests/${browser}.sh '${stringyJson}'"

        sh(script: "git clone --depth 1 --branch ${branchName} https://jenkins:${TOKEN}@giteux.azure.defra.cloud/imports/${serviceName}.git")
        sh(script: "chmod 555 ./${serviceName}/integration/scripts/BrowserStackTests/${browser}.sh")

        def exitCode = sh(script: seleniumScriptToRun, returnStatus: true)
        sh(script: "exit ${exitCode}")
    }
}
