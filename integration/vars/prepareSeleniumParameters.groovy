#!/usr/bin/env groovy

/**
 * Run the selenium tests
 *
 * @param resourceGroupName The resource group to run against
 * @param testProfile The test profile to run, e.g. sanity, regression
 * @param e2e Enabled end to end tests or not
 * @param proxyUrlExtension Tests to be run against proxy feature or not
 */

import defra.pipeline.names.PoolTag
import defra.pipeline.vault.VaultKey
import groovy.json.JsonOutput

def call(String resourceGroupName, String testProfile, boolean e2e, String proxyUrlExtension, String gridPort, String zapProxyUrl) {

    def envExt
    def e2eEnabled = "true"
    def url
    def proxyIssuer
    def poolTag = "${PoolTag.getId(resourceGroupName)}"
    def notificationServiceUrl = ""
    def borderNotificationServiceUrl = ""
    def fileUploadServiceUrl = ""
    def economicOperatorServiceUrl = ""
    def bipServiceUrl = ""
    def checksServiceUrl = ""
    def inServiceMessagesServiceUrl = ""

    if (!e2e) {
        notificationServiceUrl = "https://notification-microservice-${poolTag}.azurewebsites.net"
        borderNotificationServiceUrl = "https://bordernotification-microservice-${poolTag}.azurewebsites.net"
        fileUploadServiceUrl = "https://file-upload-microservice-${poolTag}.azurewebsites.net"
        economicOperatorServiceUrl = "https://economicoperator-microservice-${poolTag}.azurewebsites.net"
        bipServiceUrl = "https://bip-microservice-${poolTag}.azurewebsites.net"
        e2eEnabled = "false"
        checksServiceUrl = "https://checks-microservice-${poolTag}.azurewebsites.net"
        inServiceMessagesServiceUrl = "https://in-service-messaging-microservice-${poolTag}.azurewebsites.net"
    }

    switch (ENVIRONMENT) {
        case "Sandpit":
            url = "https://imports-proxy${proxyUrlExtension}.azurewebsites.net"
            apiUrl = url
            envExt = poolTag
            proxyIssuer = "https://imports-proxy.azurewebsites.net"
            break
        case "sandpit-test":
            url = "https://imports-proxy.azurewebsites.net"
            apiUrl = url
            envExt = "test"
            proxyIssuer = "https://imports-proxy.azurewebsites.net"
            break
        case "SandpitASEv2":
            url = "https://importnotification-int-static-snd.azure.defra.cloud"
            apiUrl = "https://importnotification-api-static-snd.azure.defra.cloud"
            envExt = "vnet"
            proxyIssuer = "https://imports-proxy-static.azurewebsites.net"
            break
        case "sandpit-test-ase-octo-deployed":
            url = "https://importnotification-int-snd.azure.defra.cloud"
            apiUrl = "https://importnotification-api-snd.azure.defra.cloud"
            envExt = "snd"
            proxyIssuer = "https://imports-proxy-blue-snd.azurewebsites.net"
            break
        case "TST":
            url = "https://importnotification-int-tst.azure.defra.cloud"
            apiUrl = "https://importnotification-api-tst.azure.defra.cloud"
            envExt = "tst"
            proxyIssuer = "https://imports-proxy-blue-tst.azurewebsites.net"
            break
        default:
            throw new Exception("Environment not specified, or not recognised.")
            break
    }

    def soapSearchServiceURL = "${apiUrl}/soapsearch/${envExt}"
    def soapSearchServiceTest1Password = VaultKey.getSecuredValue('soapSearchServiceIsc1Password', this)
    def soapSearchServiceTest2Password = VaultKey.getSecuredValue('soapSearchServicePhilis1Password', this)

    def frontendBcpAdminUrl = "${url}/bcpadmin/${envExt}"
    def frontendBorderNotificationUrl = "${url}/bordernotification/${envExt}"
    def frontendChecksUrl = "${url}/checks/${envExt}"
    def frontendControlUrl = "${url}/control/${envExt}"
    def frontendDecisionUrl = "${url}/decision/${envExt}"
    def frontendDriverUrl = "https://frontend-driver-${poolTag}.azurewebsites.net"
    def frontendNotificationUrl = "${url}/notification/${envExt}"
    def frontendUploadUrl = "${url}/upload/${envExt}"

    def importerPassword
    def agentPassword
    def bipPassword
    def bipReadOnlyPassword
    def lvuPassword
    def lvuReadOnlyPassword
    def fsaPassword
    def authMode

    if (testProfile == "identity") {
        importerPassword = VaultKey.getSecuredValue("insImporterB2cPassword", this)
        agentPassword = VaultKey.getSecuredValue("insAgentB2cPassword", this)
        bipPassword = VaultKey.getSecuredValue("insBipB2cPassword", this)
        bipReadOnlyPassword = VaultKey.getSecuredValue("insBipReadOnlyB2cPassword", this)
        lvuPassword = VaultKey.getSecuredValue("insLvuB2cPassword", this)
        lvuReadOnlyPassword = VaultKey.getSecuredValue("insLvuReadOnlyB2cPassword", this)
        fsaPassword = VaultKey.getSecuredValue("insFsaB2cPassword", this)
        authMode = "B2C"
    } else {
        importerPassword = VaultKey.getSecuredValue("insImporterAdPassword", this)
        agentPassword = VaultKey.getSecuredValue("insAgentB2cPassword", this)
        bipPassword = VaultKey.getSecuredValue("insBipAdPassword", this)
        bipReadOnlyPassword = VaultKey.getSecuredValue('insBipAdPassword', this)
        lvuPassword = VaultKey.getSecuredValue("insLVUPassword", this)
        lvuReadOnlyPassword = VaultKey.getSecuredValue('insLVUReadOnlyPassword', this)
        fsaPassword = VaultKey.getSecuredValue("insFsaAdPassword", this)
        authMode = "AD"
    }

    def proxyCookiePassword = VaultKey.getSecuredValue("proxyCookiePassword", this)

    def privateKey
    if (url.contains("-snd") || url.contains(".azurewebsites.net")) {
        privateKey = VaultKey.getSecuredValue("proxyJwtPrivateKey-snd", this)
    } else {
        privateKey = VaultKey.getSecuredValue("proxyJwtPrivateKey-tst", this)
    }

    def browserStackAccessKey = VaultKey.getSecuredValue("browserStackAccessKey", this)

    def wiremockUrl = "https://rds-wiremock-microservice-${poolTag}.azurewebsites.net:443"
    def redisHost = "SNDIMPRDS001.redis.cache.windows.net"
    def redisPort = 6379
    def redisPassword = VaultKey.getSecuredValue("redisPassword-selenium", this)
    def chedppTempPodRemovalUtcDate = "2022/05/14T23:00:00Z"

    def json = JsonOutput.toJson([
            AUTH_MODE                                     : authMode,
            BIP_READ_ONLY_USER_PASSWORD                   : bipReadOnlyPassword,
            BIP_USER_PASSWORD                             : bipPassword,
            CHECKS_SERVICE_URL                            : checksServiceUrl,
            CHEDPP_TEMP_POD_REMOVAL_UTC_DATE              : chedppTempPodRemovalUtcDate,
            E2E_TEST_ENABLED                              : e2eEnabled,
            E2E_TEST_ID                                   : BUILD_NUMBER,
            ECONOMIC_OPERATOR_SERVICE_URL                 : economicOperatorServiceUrl,
            BIP_SERVICE_URL                               : bipServiceUrl,
            FRONTEND_BCP_ADMIN_URL                        : frontendBcpAdminUrl,
            FRONTEND_BORDER_NOTIFICATION_URL              : frontendBorderNotificationUrl,
            FRONTEND_CHECKS_URL                           : frontendChecksUrl,
            FRONTEND_CONTROL_URL                          : frontendControlUrl,
            FRONTEND_DECISION_URL                         : frontendDecisionUrl,
            FRONTEND_DRIVER_URL                           : frontendDriverUrl,
            FRONTEND_NOTIFICATION_URL                     : frontendNotificationUrl,
            FRONTEND_UPLOAD_URL                           : frontendUploadUrl,
            FSA_USER_PASSWORD                             : fsaPassword,
            IMPORTER_USER_PASSWORD                        : importerPassword,
            AGENT_USER_PASSWORD                           : agentPassword,
            LVU_READ_ONLY_USER_PASSWORD                   : lvuReadOnlyPassword,
            LVU_USER_PASSWORD                             : lvuPassword,
            NOTIFICATION_SERVICE_URL                      : notificationServiceUrl,
            BORDER_NOTIFICATION_SERVICE_URL               : borderNotificationServiceUrl,
            FILE_UPLOAD_SERVICE_URL                       : fileUploadServiceUrl,
            IN_SERVICE_MESSAGES_SERVICE_BASE_URL          : inServiceMessagesServiceUrl,
            PRIVATE_KEY                                   : privateKey,
            PROXY_COOKIE_PASSWORD                         : proxyCookiePassword,
            PROXY_ISSUER                                  : proxyIssuer,
            SOAP_SEARCH_SERVICE_URL                       : soapSearchServiceURL,
            SOAP_USER_ISC_1_PASSWORD                      : soapSearchServiceTest1Password,
            SOAP_USER_PHILIS_1_PASSWORD                   : soapSearchServiceTest2Password,
            TEST_PROFILE                                  : testProfile,
            WIREMOCK_URL                                  : wiremockUrl,
            REDIS_HOST                                    : redisHost,
            REDIS_PORT                                    : redisPort,
            REDIS_PASSWORD                                : redisPassword,
            SELENIUM_GRID_PORT                            : gridPort,
            BROWSER_STACK_ACCESS_KEY                      : browserStackAccessKey,
            ZAP_PROXY_URL                                 : zapProxyUrl
    ])

    return json
}
