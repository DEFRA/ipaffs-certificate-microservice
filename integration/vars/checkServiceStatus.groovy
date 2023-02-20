import defra.pipeline.config.Config

def call(String secretName, String vaultName, String serviceName, String resourceGroup) {
    def parametersMap = Config.getParameters(serviceName, environment.toLowerCase(), this)
    def user = 'importer'
    def basicAuthPassword = getVaultKey(secretName, 'importsKeyVault')
    def dnsSuffix = parametersMap.parameters.dnsSuffix ? parametersMap.parameters.dnsSuffix.value : '.azurewebsites.net'
    def serviceNameFull = getNameWithPoolTag(serviceName, resourceGroup)
    timeout(time:5, unit: 'MINUTES') {
        waitUntil {
            try {
                echo '========= Checking For Service Status =========='
                echo "curl -sk -m 10 --user ${user}:${basicAuthPassword} https://${serviceNameFull}/admin/health-check | jq -rj .status"
                def serviceStatus = sh(script: "curl -sk -m 10 --user ${user}:${basicAuthPassword} https://${serviceNameFull}${dnsSuffix}/admin/health-check | jq -rj '.status'", returnStdout: true)
                echo serviceStatus
                return serverStatus == 'UP'
            } catch (Exception e) {
                if (e.getMessage() == 'script returned exit code 7') {
                    echo 'Connection refused, waiting till server starts.'
                    return false
                } else if (e.getMessage() == 'script returned exit code 35') {
                    echo 'Unknown SSL error, waiting till server starts.'
                    return false
                } else if (e.getMessage() == 'script returned exit code 28') {
                    echo 'Curl timed out.'
                    return false
                } else if (e.getMessage() == 'script returned exit code 6') {
                    echo 'Could not resolve host.'
                    return false
                }
            }
        }
    }
}
