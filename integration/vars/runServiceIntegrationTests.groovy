#!/usr/bin/env groovy

/**
 * Run the integration tests
 *
 * @param serviceName        The frontend service to run selenium tests against
 * @param subscription       The subscription, e.g. Sandpit
 * @param branchName         The branch to run tests from
 * @param resourceGroupName  The resource group to run against
 * @param testProfile        The test profile to run, e.g. sanity, regression
 */

import defra.pipeline.config.Config
import defra.pipeline.vault.VaultKey
import defra.pipeline.names.PoolTag
import defra.pipeline.names.Branches

def call(String serviceName, String subscription, String branchName, String resourceGroupName, String testProfile) {

  def parametersMap = Config.getParameters(serviceName, subscription.toLowerCase(), this)
  def userName = 'importer'

  def password = ''
  if (serviceName.startsWith("permissions") || serviceName.startsWith("enotification-submission")) {
      password = VaultKey.getSecuredValue("${serviceName}BasicAuthPassword", this)
  }

  def fullServiceName = PoolTag.getNameWithTag(serviceName, resourceGroupName)
  def dnsSuffix = parametersMap.parameters.dnsSuffix ? parametersMap.parameters.dnsSuffix.value : '.azurewebsites.net'
  def azureName = 'SNDIMPINFSBS002-dev'
  def azureEntityPath = 'integration_ipaffs_queue'
  def azureSharedAccessKeyName = VaultKey.getSecuredValue("devServiceBusSharedAccessKeyName", this)
  def sharedAccessKey = VaultKey.getSecuredValue("devServiceBusSharedAccessKey", this)
  def baseUrl = "https://${fullServiceName}${dnsSuffix}"
  def branchPrefix = Branches.getBranchPrefix("${branchName}")
  def testOpenidTokenServiceURL = "https://openid-token-microservice.azurewebsites.net/"
  def testOpenidTokenServiceBasicAuthUsername = "poc"
  def testOpenidTokenServiceBasicAuthPassword = VaultKey.getSecuredValue("testOpenidTokenServiceBasicAuthPassword", this)
  def envSuffix = PoolTag.getId(resourceGroupName)
  def notificationURL = "https://notification-microservice-${envSuffix}${dnsSuffix}"
  def checksURL = "https://checks-microservice-${envSuffix}${dnsSuffix}"
  def economicOperatorURL = "https://economicoperator-microservice-${envSuffix}${dnsSuffix}"
  def fileUploadURL = "https://file-upload-microservice-${envSuffix}${dnsSuffix}"
  def frontendNotificationURL = "https://frontend-notification-${envSuffix}${dnsSuffix}"
  def submissionURL = "https://enotification-submission-microservice-${envSuffix}${dnsSuffix}"
    //Tech Debt -- this needs revisting, soap service integration tests need mocks adding so don't
    //have to pass these, and this method can become generic again
    def cmd

    if (serviceName.startsWith("soapsearch-microservice")) {
        def ics1Password = VaultKey.getSecuredValue("soapSearchServiceIsc1Password", this)
        def ics2Password = VaultKey.getSecuredValue("soapSearchServiceIsc2Password", this)
        def philis1Password = VaultKey.getSecuredValue("soapSearchServicePhilis1Password", this)
        def notificationAuthPassword = VaultKey.getSecuredValue("notification-microserviceBasicAuthPassword", this)

        cmd = """
            set +x;
            mvn clean verify -P ${testProfile} -f integration/pom.xml \
            --settings ./settings/maven.xml \
            -Dskip.integration.tests=false \
            -Dservice.base.url=${baseUrl} \
            -Dauth.username=${userName} \
            -Dauth.password=${password} \
            -Dsoap.search.service.isc.1.password=${ics1Password} \
            -Dsoap.search.service.isc.2.password=${ics2Password} \
            -Dsoap.search.service.philis.1.password=${philis1Password} \
            -Dnotification.service.base.url=${notificationURL} \
            -Dnotification.auth.password=${notificationAuthPassword} \
            -Denvironment.name=${subscription} \
            -Dtest.openid.service.url=${testOpenidTokenServiceURL} \
            -Dtest.openid.service.auth.username=${testOpenidTokenServiceBasicAuthUsername} \
            -Dtest.openid.service.auth.password=${testOpenidTokenServiceBasicAuthPassword} \
            -Dbranch.prefix=${branchPrefix} > logs.out 2>&1
            """

    } else if (serviceName.startsWith("notification-microservice")) {

        def azureSearchNotificationIndexName = envSuffix + "-" + parametersMap.parameters.azureSearchIndexSuffix.value
        def azureSearchQueryKey = VaultKey.getSecuredValue("imports-tests-azure-search-query-key", this)
        def azureSearchApiVersion = parametersMap.parameters.azureSearchApiVersion.value
        def azureSearchServiceName = parametersMap.parameters.azureSearchServiceName.value
        def serviceBusTopicName = (branchName.startsWith("hotfix")) ? "hotfix-notification-topic" : "sandpit-${envSuffix}-notification-topic"

        cmd = """
        set +x;
        mvn clean verify -P ${testProfile} -f integration/pom.xml \
        --settings ./settings/maven.xml \
        -Dskip.integration.tests=false \
        -Dservice.base.url=${baseUrl} \
        -Dauth.username=${userName} \
        -Dauth.password=${password} \
        -Denvironment.name=${subscription} \
        -Dbranch.prefix=${branchPrefix} \
        -Dazure.search.query.key=${azureSearchQueryKey} \
        -Dazure.search.api.version=${azureSearchApiVersion} \
        -Dazure.search.service.name=${azureSearchServiceName} \
        -Dazure.search.index.name=${azureSearchNotificationIndexName} \
        -Dtest.openid.service.url=${testOpenidTokenServiceURL} \
        -Dtest.openid.service.auth.username=${testOpenidTokenServiceBasicAuthUsername} \
        -Dtest.openid.service.auth.password=${testOpenidTokenServiceBasicAuthPassword} \
        -Dazure.environment.name=${azureName} \
        -Dazure.service.bus.queue.name=${azureEntityPath} \
        -Dazure.service.bus.topic.name=${serviceBusTopicName} \
        -Dazure.shared.access.key.name=${azureSharedAccessKeyName} \
        -Dazure.shared.access.key=${sharedAccessKey} > logs.out 2>&1
        """

    } else if (serviceName.startsWith("referencedataloader-microservice") || serviceName.startsWith('cloning-microservice')) {

        cmd = """
            set +x;
            mvn clean verify -P ${testProfile} -f integration/pom.xml \
            --settings ./settings/maven.xml \
            -Dskip.integration.tests=false \
            -Dservice.base.url=${baseUrl} \
            -Dnotification.service.base.url=${notificationURL} \
            -Deconomicoperator.service.base.url=${economicOperatorURL} \
            -Dtest.openid.service.url=${testOpenidTokenServiceURL} \
            -Dtest.openid.service.auth.username=${testOpenidTokenServiceBasicAuthUsername} \
            -Dtest.openid.service.auth.password=${testOpenidTokenServiceBasicAuthPassword} \
            -Dwiremock.scheme=https \
            -Dwiremock.host="rds-wiremock-microservice-${envSuffix}${dnsSuffix}" \
            -Dwiremock.port=443 > logs.out 2>&1
            """

    } else if (serviceName.startsWith("economicoperator-microservice") || serviceName.startsWith("bip-microservice")) {

        cmd = """
        set +x;
        mvn clean verify -P ${testProfile} -f integration/pom.xml \
        --settings ./settings/maven.xml \
        -Dskip.integration.tests=false \
        -Dservice.base.url=${baseUrl} \
        -Dfile.service.base.url=${fileUploadURL} \
        -Dauth.username=${userName} \
        -Dauth.password=${password} \
        -Denvironment.name=${subscription} \
        -Dbranch.prefix=${branchPrefix} \
        -Dtest.openid.service.url=${testOpenidTokenServiceURL} \
        -Dtest.openid.service.auth.username=${testOpenidTokenServiceBasicAuthUsername} \
        -Dtest.openid.service.auth.password=${testOpenidTokenServiceBasicAuthPassword} > logs.out 2>&1
        """

    } else if (serviceName.startsWith("risk-interface-microservice")) {
        cmd = """
        set +x;
        mvn clean verify -P ${testProfile} -f integration/pom.xml \
        --settings ./settings/maven.xml \
        -Dskip.integration.tests=false \
        -Dservice.base.url=${baseUrl} \
        -Dnotification.service.base.url=${notificationURL} \\
        -Dchecks.service.base.url=${checksURL} \\
        -Dauth.username=${userName} \
        -Dauth.password=${password} \
        -Denvironment.name=${subscription} \
        -Dbranch.prefix=${branchPrefix} \
        -Dtest.openid.service.url=${testOpenidTokenServiceURL} \
        -Dtest.openid.service.auth.username=${testOpenidTokenServiceBasicAuthUsername} \
        -Dtest.openid.service.auth.password=${testOpenidTokenServiceBasicAuthPassword} > logs.out 2>&1
        """

    } else if (serviceName.startsWith("certificate-microservice")) {
        cmd = """
        set +x;
        mvn clean verify -P ${testProfile} -f integration/pom.xml \
        --settings ./settings/maven.xml \
        -Dskip.integration.tests=false \
        -Dservice.base.url=${baseUrl} \
        -Dfrontend.notification.base.url=${frontendNotificationURL} \
        -Dauth.username=${userName} \
        -Dauth.password=${password} \
        -Denvironment.name=${subscription} \
        -Dbranch.prefix=${branchPrefix} \
        -Dtest.openid.service.url=${testOpenidTokenServiceURL} \
        -Dtest.openid.service.auth.username=${testOpenidTokenServiceBasicAuthUsername} \
        -Dtest.openid.service.auth.password=${testOpenidTokenServiceBasicAuthPassword} > logs.out 2>&1
        """

    } else if (serviceName.startsWith("risk-locking-microservice")) {
        cmd = """
        set +x;
        mvn clean verify -P ${testProfile} -f integration/pom.xml \
        --settings ./settings/maven.xml \
        -Dskip.integration.tests=false \
        -Dnotification.service.base.url=${notificationURL} \
        -Denvironment.name=${subscription} \
        -Dbranch.prefix=${branchPrefix} \
        -Dtest.openid.service.url=${testOpenidTokenServiceURL} \
        -Dtest.openid.service.auth.username=${testOpenidTokenServiceBasicAuthUsername} \
        -Dtest.openid.service.auth.password=${testOpenidTokenServiceBasicAuthPassword} > logs.out 2>&1
        """

    } else if (serviceName.startsWith("auto-clearance-microservice")) {
        cmd = """
        set +x;
        mvn clean verify -P ${testProfile} -f integration/pom.xml \
        --settings ./settings/maven.xml \
        -Dskip.integration.tests=false \
        -Dnotification.service.base.url=${notificationURL} \
        -Denvironment.name=${subscription} \
        -Dbranch.prefix=${branchPrefix} \
        -Dtest.openid.service.url=${testOpenidTokenServiceURL} \
        -Dtest.openid.service.auth.username=${testOpenidTokenServiceBasicAuthUsername} \
        -Dtest.openid.service.auth.password=${testOpenidTokenServiceBasicAuthPassword} > logs.out 2>&1
        """


    } else if (serviceName.startsWith("risk-assessment-microservice")) {
        cmd = """
        set +x;
        mvn clean verify -P ${testProfile} -f integration/pom.xml \
        --settings ./settings/maven.xml \
        -Dskip.integration.tests=false \
        -Dservice.base.url=${baseUrl} \
        -Dchecks.service.base.url=${checksURL} \
        -Denvironment.name=${subscription} \
        -Dbranch.prefix=${branchPrefix} \
        -Dtest.openid.service.url=${testOpenidTokenServiceURL} \
        -Dtest.openid.service.auth.username=${testOpenidTokenServiceBasicAuthUsername} \
        -Dtest.openid.service.auth.password=${testOpenidTokenServiceBasicAuthPassword} > logs.out 2>&1
        """


    } else if (serviceName.startsWith("enotification-processing-microservice")) {
        cmd = """
        set +x;
        mvn clean verify -P ${testProfile} -f integration/pom.xml \
        --settings ./settings/maven.xml \
        -Dskip.integration.tests=false \
        -Dservice.base.url=${baseUrl} \
        -Devent.base.url=${submissionURL} \
        -Deconomic.operator.base.url=${economicOperatorURL} \
        -Dauth.username=${userName} \
        -Dauth.password=${password} \
        -Denvironment.name=${subscription} \
        -Dbranch.prefix=${branchPrefix} \
        -Dtest.openid.service.url=${testOpenidTokenServiceURL} \
        -Dtest.openid.service.auth.username=${testOpenidTokenServiceBasicAuthUsername} \
        -Dtest.openid.service.auth.password=${testOpenidTokenServiceBasicAuthPassword} > logs.out 2>&1
        """


    } else {
        cmd = """
        set +x;
        mvn clean verify -P ${testProfile} -f integration/pom.xml \
        --settings ./settings/maven.xml \
        -Dskip.integration.tests=false \
        -Dservice.base.url=${baseUrl} \
        -Dauth.username=${userName} \
        -Dauth.password=${password} \
        -Denvironment.name=${subscription} \
        -Dbranch.prefix=${branchPrefix} \
        -Dtest.openid.service.url=${testOpenidTokenServiceURL} \
        -Dtest.openid.service.auth.username=${testOpenidTokenServiceBasicAuthUsername} \
        -Dtest.openid.service.auth.password=${testOpenidTokenServiceBasicAuthPassword} > logs.out 2>&1
        """
    }

    try {
        sh(script: cmd)
    } catch (Exception e) {
        sh script: 'cat logs.out && rm -f logs.out'
        throw e
    }

    sh script: 'rm -f logs.out'
    echo "Successfully ran integration tests"
}
