package defra.pipeline.azure

import defra.pipeline.config.Config
import defra.pipeline.environments.ServiceVersionMismatchException
import defra.pipeline.environments.WaitForVersion
import defra.pipeline.names.PoolTag
import defra.pipeline.script.ScriptActions
import hudson.AbortException

class AzureActions {

    /**
     * Create a resource group in Azure.
     *
     * @param resourceGroupName  The name of the resource group to create
     * @param script             The global script parameter
     */
    public static void createResourceGroup(String resourceGroupName, Script script) {

        def dateTimeObject = new Date()
        def currentDateTime = dateTimeObject.format("yyMMdd.HHmm", TimeZone.getTimeZone('UTC'))
        def createCommand = "az group create -n ${resourceGroupName} -l \"North Europe\" --tags CreationDateTime=\"${currentDateTime}\""

        ScriptActions.runCommandLogOnlyOnError(createCommand, script)

    }

    public static void deleteResourceGroup(String resourceGroupName, boolean wait, Script script) {

        script.echo("DELETING RESOURCE GROUP: ${resourceGroupName}")

        if (wait) {
            def deleteCommand = "az group delete --name ${resourceGroupName} --yes --verbose"

            // Retry 3 times with a 5 minute sleep in between as we sometimes get
            // errors from Azure indicating we should retry.
            for (int i = 0; i < 3; i++) {
                def out
                try {
                    out = script.sh(script: deleteCommand, returnStdout: true)
                } catch (Exception e) {
                    script.echo("""Error: ${out}""")
                    script.sleep(300)
                    continue
                }
                break
            }
        }
        else {
            def deleteCommand = "az group delete --name ${resourceGroupName} --yes --verbose  --no-wait"
            script.sh(script: deleteCommand, returnStdout: true)
        }
    }

    /**
     * Assign standard roles for a resource group
     *
     * @param resourceGroupName  The name of the resource group to create
     * @param script             The global script parameter
     */
    public static void assignRoles(String resourceGroupName, Script script) {

        def azureSubscriptionId = Config.getPropertyValue("azureSubscriptionId", script)

        def azureContributorActiveDirectoryGroup = Config.getPropertyValue("azureContributorActiveDirectoryGroup", script)
        def roleScript = "az role assignment create --role \"Contributor\" --assignee-object-id \"${azureContributorActiveDirectoryGroup}\" --scope \"/subscriptions/${azureSubscriptionId}/resourceGroups/${resourceGroupName}\""
        ScriptActions.runCommandLogOnlyOnError(roleScript, script)

        def azureSndImpContributorActiveDirectoryGroup = Config.getPropertyValue("azureSndImpContributorActiveDirectoryGroup", script)
        roleScript = "az role assignment create --role \"Contributor\" --assignee-object-id \"${azureSndImpContributorActiveDirectoryGroup}\" --scope \"/subscriptions/${azureSubscriptionId}/resourceGroups/${resourceGroupName}\""
        ScriptActions.runCommandLogOnlyOnError(roleScript, script)

        def azureEuxIpaffsKainosDevUsersActiveDirectoryGroup = Config.getPropertyValue("azureEuxIpaffsKainosDevUsersActiveDirectoryGroup", script)
        roleScript = "az role assignment create --role \"Contributor\" --assignee-object-id \"${azureEuxIpaffsKainosDevUsersActiveDirectoryGroup}\" --scope \"/subscriptions/${azureSubscriptionId}/resourceGroups/${resourceGroupName}\""
        ScriptActions.runCommandLogOnlyOnError(roleScript, script)

        def azureOwnerActiveDirectoryGroup = Config.getPropertyValue("azureOwnerActiveDirectoryGroup", script)
        roleScript = "az role assignment create --role \"Owner\" --assignee-object-id \"${azureOwnerActiveDirectoryGroup}\" --scope \"/subscriptions/${azureSubscriptionId}/resourceGroups/${resourceGroupName}\""
        ScriptActions.runCommandLogOnlyOnError(roleScript, script)

        def azureReaderActiveDirectoryGroup = Config.getPropertyValue("azureReaderActiveDirectoryGroup", script)
        roleScript = "az role assignment create --role \"Reader\" --assignee-object-id \"${azureReaderActiveDirectoryGroup}\" --scope \"/subscriptions/${azureSubscriptionId}/resourceGroups/${resourceGroupName}\""
        ScriptActions.runCommandLogOnlyOnError(roleScript, script)

    }

    /**
     * Assign standard tags for a resource group
     *
     * @param resourceGroupName  The name of the resource group to create
     * @param script             The global script parameter
     */
    public static void assignTags(String resourceGroupName, Script script) {

        def buildPoolTag = Config.getPropertyValue("projectName", script)
        def resourceGroupsServiceCode = Config.getPropertyValue("resourceGroupsServiceCode", script)
        def resourceGroupsServiceName = Config.getPropertyValue("resourceGroupsServiceName", script)
        def resourceGroupsServiceType = Config.getPropertyValue("resourceGroupsServiceType", script)

        def setTagsOnResourceGroup = """
        az group update --name ${resourceGroupName} --set \
        tags.Name=${resourceGroupName} \
        tags.ServiceCode=\"${resourceGroupsServiceCode}\" \
        tags.ServiceName=\"${resourceGroupsServiceName}\" \
        tags.ServiceType=\"${resourceGroupsServiceType}\" \
        tags.Environment=SND \
        tags.Tier=\"ResourceGroup\" \
        tags.Region=\"North Europe\" \
        """.trim()

        ScriptActions.runCommandLogOnlyOnError(setTagsOnResourceGroup, script)
    }

    /**
     * Deploy a component in Azure
     *
     * @param resourceGroupName      The name of the resource group to deploy
     * @param serviceName            The name of the service
     * @param template               The arm template name
     * @param subscription           The subscription, e.g. Sandpit
     * @param containerRepository    The repository within azure container registry in which the image is stored
     * @param expectedVersion        The version of the service to be deployed
     * @param script                 The global script parameter
     */
    public static void deployComponent(String resourceGroupName, String serviceName, String template, String subscription, String containerRepository, String expectedVersion, Script script) {

        script.echo("Deploying Component: " + serviceName)

        script.getFile("configuration/imports/web_app_services/templates/${template}.json")
        script.getFile("configuration/imports/web_app_services/parameters/${subscription.toLowerCase()}/${template}.parameters.json")

        def dateTimeObject = new Date()
        def currentDateTime = dateTimeObject.format("yyMMdd.HHmm", TimeZone.getTimeZone('UTC'))

        def serviceNameFull = PoolTag.getNameWithTag(serviceName, resourceGroupName)
        def envCode = (resourceGroupName.contains ('Hotfix')) ? 'HFX' : 'SND'
        def envSuffix = PoolTag.getId(resourceGroupName)

        try {
            def scriptToRun = """ \
                az deployment group create -g $resourceGroupName --name ${serviceNameFull} --template-file configuration/imports/web_app_services/templates/${template}.json \
                 --parameters configuration/imports/web_app_services/parameters/${subscription.toLowerCase()}/${template}.parameters.json \
                 --parameters \"dateCreated=${currentDateTime}\" \"containerRepository=${containerRepository}\" \"envCode=${envCode}\" \"envSuffix=${envSuffix}\" \"version=${expectedVersion}\" \
                 ${if (serviceName.contains('proxy')){return ""} else {return "\"serviceName=${serviceNameFull}\""}} \
                 """.trim()

            ScriptActions.runCommandLogOnlyOnError(scriptToRun, script)

            if (!serviceName.contains("antivirus-stub")) {
                script.echo("DEPLOYED ${serviceName} TO ${resourceGroupName}, CHECKING CORRECT VERSION DEPLOYED")

                def deployed = timedWaitForVersion(resourceGroupName, serviceName, subscription, expectedVersion, script)

                if (deployed) {
                    script.echo "----DEPLOYED VERSION OF ${serviceName} CORRECTLY MATCHES ${expectedVersion}---"
                } else {
                    throw new ServiceVersionMismatchException("Expected version ${expectedVersion} did not match for ${serviceNameFull} after maximum attempts")
                }
            } else {
                script.echo("DEPLOYED ${serviceName} TO ${resourceGroupName}")
            }
        } catch (Exception ex) {
            script.echo(ex.getMessage())
            throw new AbortException("Failed to deploy ${serviceNameFull}")
        }
    }

    private static boolean timedWaitForVersion(String resourceGroupName, String serviceName, String subscription, String expectedVersion, Script script) {
        return WaitForVersion.checkVersion(serviceName, resourceGroupName, subscription, expectedVersion, script)
    }

    /**
     * Deploy a Application Insights in Azure
     *
     * @param resourceGroupName      The name of the resource group to deploy
     * @param template               The arm template name (application-insights)
     * @param subscription           The subscription, e.g. Sandpit
     * @param script                 The global script parameter
     */
    public static void deployApplicationInsightsComponent(String resourceGroupName, String template, String subscription, Script script) {
        script.getFile("configuration/imports/application_insights/templates/${template}.json")
        script.getFile("configuration/imports/application_insights/parameters/${subscription.toLowerCase()}/${template}.parameters.json")

        def dateTimeObject = new Date()
        def currentDateTime = dateTimeObject.format("yyMMdd.HHmm", TimeZone.getTimeZone('UTC'))

        def componentName = "insights-" + resourceGroupName
        def envSuffix = PoolTag.getId(resourceGroupName)


        def scriptToRun = """ \
        az deployment group create -g $resourceGroupName --name ${componentName} --template-file configuration/imports/application_insights/templates/${template}.json \
         --parameters configuration/imports/application_insights/parameters/${subscription.toLowerCase()}/${template}.parameters.json \
         --parameters \"envSuffix=${envSuffix}\" \"dateCreated=${currentDateTime}\" 
        """.trim()
        ScriptActions.runCommandLogOnlyOnError(scriptToRun, script)

        script.echo("DEPLOYED ${componentName} TO ${resourceGroupName}")
    }

      /**
     * Deploy App Service Plan in Azure
     *
     * @param resourceGroupName      The name of the resource group to deploy
     * @param appServicePlanGroup    The name of the app service plan /file (services-general)
     * @param subscription           The subscription, e.g. Sandpit
     * @param script                 The global script parameter
     */
    public static void deployAppServicePlanComponent(String resourceGroupName, String appServicePlanGroup, String subscription, Script script) {
        script.getFile("configuration/imports/appserviceplans/templates/appserviceplan.json")
        script.getFile("configuration/imports/appserviceplans/parameters/${subscription.toLowerCase()}/${appServicePlanGroup}.parameters.json")

        def dateTimeObject = new Date()
        def currentDateTime = dateTimeObject.format("yyMMdd.HHmm", TimeZone.getTimeZone('UTC'))
        def envSuffix = PoolTag.getId(resourceGroupName)

        def scriptToRun = """ \
        az deployment group create -g $resourceGroupName --name $appServicePlanGroup --template-file configuration/imports/appserviceplans/templates/appserviceplan.json \
         --parameters configuration/imports/appserviceplans/parameters/${subscription.toLowerCase()}/${appServicePlanGroup}.parameters.json \
         --parameters \"envSuffix=${envSuffix}\" \"dateCreated=${currentDateTime}\"
        """.trim()
        ScriptActions.runCommandLogOnlyOnError(scriptToRun, script)

        script.echo("DEPLOYED ${appServicePlanGroup} TO ${resourceGroupName}")
    }

    /**
     * Validate ARM templates for errors
     *
     * @param subscription           The subscription in which the parameter files are used for deployment e.g. Sandpit
     * @param template               The name of the template that matches the corresponding parameter file
     * @param script                 The global script parameter
     */

    public static void validateArmTemplate(String subscription, String template, Script script) {
        script.getFile("configuration/imports/web_app_services/templates/${template}.json")
        script.getFile("configuration/imports/web_app_services/parameters/${subscription.toLowerCase()}/${template}.parameters.json")

        def resourceGroupName = subscription == 'sandpit' ? 'SNDIMPINFRGP001-imports-static-test' : 'SNDIMPINFRGP001-imports-static-vnet'
        def output = script.sh(script: "az deployment group validate -g ${resourceGroupName} --template-file configuration/imports/web_app_services/templates/${template}.json --parameters configuration/imports/web_app_services/parameters/${subscription.toLowerCase()}/${template}.parameters.json --parameters \"envSuffix=test\" --query error", returnStdout: true).trim()

        if (output.contains('code')) {
            throw new Exception("Validation had Failed for ${template}!!!! Error deatails below: ${output}")
        } else {
            script.echo "Validation succeded for ${template}"
        }
    }

    /**
     * Compares two parameter files so that parameters match to use one template
     *
     * @param template               The name of the template that matches the corresponding parameter file
     * @param subscription1          The subscription in which the parameter files are used for deployment e.g. Sandpit
     * @param subscription2          The subscription in which the parameter files are used for deployment e.g. Octopus
     * @param script                 The global script parameter
     */

    public static void compareArmParameters(String template, String subscription1, String subscription2, Script script) {
        def parameterFile1 = Config.getParameters(template, subscription1, script).parameters.keySet()
        def parameterFile2 = Config.getParameters(template, subscription2, script).parameters.keySet()

        if (parameterFile1 == parameterFile2) {
          script.echo("${subscription1}/${template}.parameters.json Matches ${subscription2}/${template}.parameters.json : SUCCESS")
        } else {
          def dif = parameterFile1 - parameterFile2
          script.echo("${subscription1}/${template}.parameters.json Parameters DOES NOT MATCH ${subscription2}/${template}.parameters.json")
          script.echo("MISSING PARAMETERS: ${dif}")
          throw new Exception("FAILURE: Parameters Missing for ${template} in ${subscription1} and ${subscription2}")
        }
    }
    /**
     * Deletes image or repository from ACR
     *
     * @param acrName                The name of the ACR to perform the action eg sndeuxfesacr001
     * @param repositoryOrImage     The command to pass in to determine if your deleting a repository or an image located in the acr
     * @param script                 The global script parameter
     * @param acrImage               The name of the image or repository to be deleted
     */

    public static void destroyACR(String acrName, String repositoryOrImage, Script script, String acrImage) {
        script.echo("DELETING ${acrImage} from ${acrName}")
        def deleteACRcommand = """
        set +x
        az acr repository delete --name=${acrName} --${repositoryOrImage}=${acrImage} -y || true
        """
        ScriptActions.runCommandLogOnlyOnError(deleteACRcommand, script)
    }
}
