package defra.pipeline.testing

import defra.pipeline.config.Config

class Zap {
    static void start(String networkName, String containerName, String port, Script script) {
        script.echo("Starting ZAP")

        def zapImage = "${Config.getPropertyValue("azureSndContainerRegistry", script)}/${Config.getPropertyValue("zapBaseImage", script)}"

        script.sh("docker run --name ${containerName} -p ${port}:${port} --network ${networkName} -d ${zapImage} -port ${port}")

        // Check that ZAP is running before continuing (fail if not)
        script.retry(10) {
            script.sleep(10)
            script.sh('docker ps')
            script.sh("curl -s http://localhost:${port}")
        }

        script.echo("ZAP is running at http://localhost:${port}")
    }

    static void stop(String containerName, String networkName, Script script) {
        script.sh("docker container rm -f ${containerName}")
    }

    static void publishReport(String port, Script script) {
        script.sh("curl -s -o zap-report.html http://localhost:${port}/OTHER/core/other/htmlreport?apikey=super-secret-key")
        script.publishHTML(target: [
                reportDir: '.',
                reportFiles: 'zap-report.html',
                reportName: 'ZAP Report',
                keepAll: true,
                alwaysLinkToLastBuild: true,
                allowMissing: true
        ])
    }
}
