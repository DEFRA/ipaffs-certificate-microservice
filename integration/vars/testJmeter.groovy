#!/usr/bin/env groovy
import defra.pipeline.config.Config

def call(Map parameters, String baseTemplate, String populatedTemplate, String branch, String token) {
    // performance tests will be calling this multiple times only want to download once
    if (!fileExists('test')) {
        sh(script: "git clone --branch ${branch} --depth 1 https://jenkins:${token}@giteux.azure.defra.cloud/imports/test.git")
    }

    String path = sh(script: 'pwd', returnStdout: true).trim()

    String template = readFile("test/jmeter/${baseTemplate}")
    String populated = prepareJMeterParameters(parameters, template)
    writeFile(file: "test/jmeter/${populatedTemplate}", text: populated)

    String bztImage = "${Config.getPropertyValue("azureSndContainerRegistry", this)}/${Config.getPropertyValue("blazematerBaseImage", this)}"

    sh(script: "docker run --rm -v ${path}/test/jmeter:/bzt-configs -v ${path}:/tmp/artifacts ${bztImage} ${populatedTemplate} -report")
}
