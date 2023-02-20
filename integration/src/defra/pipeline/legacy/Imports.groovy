package defra.pipeline.legacy

import defra.pipeline.vault.VaultKey

class Imports {

    public static String getUrlForSelenium(String environmentSuffix, String application, Script script, Boolean withBasicAuth = false, String proxyUrlExtension = "") {

        def applicationURL = [
            "frontend-notification": "imports-proxy${proxyUrlExtension}",
            "frontend-decision": "imports-proxy${proxyUrlExtension}",
            "frontend-control": "imports-proxy${proxyUrlExtension}",
            "frontend-checks": "imports-proxy${proxyUrlExtension}",
            "frontend-bordernotification": "imports-proxy${proxyUrlExtension}",
            "notificationService": "notification-microservice-${environmentSuffix}",
            "decisionService": "decision-microservice-${environmentSuffix}",
            "soapSearchService": "soapsearch-microservice-${environmentSuffix}"
        ]

        def url = 'https://'

        if (withBasicAuth) {
            def applicationBasicAuthUsernames = [
                "notificationService": "anything",
                "decisionService": "anything",
                "soapSearchService": "anything"
            ]
            def applicationBasicAuthPassword = VaultKey.getSecuredValue('mainfrontend-microserviceBasicAuthPassword', script)
            url += "${applicationBasicAuthUsernames[application]}:${applicationBasicAuthPassword}@"
        }
        // TODO: add if to seperate ASEv2 and normal envs: url += applicationURL[application] + ".azurewebsites.net"
        // url += applicationURL[application] + ".imp.snd.azure.defra.cloud"

        url += applicationURL[application] + ".azurewebsites.net"

        if (application == "frontend-notification") {
            url += "/notification/${environmentSuffix}"
        } else if (application == "frontend-decision") {
            url += "/decision/${environmentSuffix}"
        } else if (application == "frontend-control") {
            url += "/control/${environmentSuffix}"
        } else if (application == "frontend-checks") {
            url += "/checks/${environmentSuffix}"
        } else if (application == "frontend-bordernotification") {
            url += "/bordernotification/${environmentSuffix}"
        }

        return url
    }
}
