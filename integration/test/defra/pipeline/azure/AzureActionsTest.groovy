package defra.pipeline.azure

import defra.pipeline.BaseTest
import defra.pipeline.environments.WaitForVersion
import hudson.AbortException
import org.junit.Test

class AzureActionsTest extends BaseTest {

    @Test
    public void testCreateResourceGroup() {
        AzureActions.createResourceGroup("SNDIMPINFRGP001-Pool-12", this)

        def createCommandsRan = testCommandRan "az group create.*"
        assert createCommandsRan.size() == 1
        assert createCommandsRan[0].matches(".* -n SNDIMPINFRGP001-Pool-12.*")
    }

    @Test
    public void testAssignRoles() {
        AzureActions.assignRoles("SNDIMPINFRGP001-Pool-12", this)

        def assignRolesCommandsRan = testCommandRan "az role assignment create.*"
        assert assignRolesCommandsRan.size() == 5
    }

    @Test
    public void testAssignTags() {
        AzureActions.assignTags("SNDIMPINFRGP001-Pool-12", this)

        def assignTagsCommandsRan = testCommandRan "az group update.*"
        assert assignTagsCommandsRan.size() == 1
    }

    @Test
    public void testDeployComponentWithAppInsights() {
        WaitForVersion.metaClass.static.checkVersion = { String serviceName, String resourceGroupName, String subscription, String expectedVersionNumber, Script script -> true}

        AzureActions.deployComponent("SNDIMPINFRGP001-Pool-12", "notification-microservice", "notification-microservice",
                                     "Sandpit", "imports-imta-111", "1.0.0", this)

        assert filesGot.contains("configuration/imports/web_app_services/templates/notification-microservice.json")
        assert filesGot.contains("configuration/imports/web_app_services/parameters/sandpit/notification-microservice.parameters.json")

        def deployCommandsRan = testCommandRan ".*az deployment group create.*"
        assert deployCommandsRan.size() == 1
        assert deployCommandsRan[0].contains("-g SNDIMPINFRGP001-Pool-12")
        assert deployCommandsRan[0].contains("--name notification-microservice-12")
        assert deployCommandsRan[0].contains("--template-file configuration/imports/web_app_services/templates/notification-microservice.json")
        assert deployCommandsRan[0].contains("--parameters configuration/imports/web_app_services/parameters/sandpit/notification-microservice.parameters.json")
        assert deployCommandsRan[0].contains("version=1.0.0")
        assert deployCommandsRan[0].contains("containerRepository=imports-imta-111")
        assert deployCommandsRan[0].contains("serviceName=notification-microservice-12")
        assert deployCommandsRan[0].contains("envSuffix=12")

        WaitForVersion.metaClass = null
    }

    @Test
    public void testDeployComponent_Aborts_WhenWaitForVersionReturnsFalse() {
        WaitForVersion.metaClass.static.checkVersion = { String serviceName, String resourceGroupName, String subscription, String expectedVersionNumber, Script script -> false}

        def thrownException = false

        try {
            AzureActions.deployComponent("SNDIMPINFRGP001-Pool-12", "notification-microservice", "notification-microservice",
                    "Sandpit", "imports-imta-111", "1.0.0", this)
        } catch (AbortException ex) {
            thrownException = true
        }

        WaitForVersion.metaClass = null
        assert thrownException
    }

    @Test
    public void testDeployComponentWithRetry() {
        WaitForVersion.metaClass.static.checkVersion = { String serviceName, String resourceGroupName, String subscription, String expectedVersionNumber, Script script -> true}

        shellCommandsThrow["az  deployment group create.*"] = "ABCD123456"

        def thrownException = false

        try {
            AzureActions.deployComponent("SNDIMPINFRGP001-Pool-12", "notification-microservice", "notification-microservice",
                    "Sandpit", "imports-imta-111", "1.0.0", this)
        } catch (AbortException ex) {
            thrownException = true
        }

        WaitForVersion.metaClass = null
        assert !thrownException
    }

    @Test
    public void testDeployApplicationInsightsComponent() {
        AzureActions.deployApplicationInsightsComponent("SNDIMPINFRGP001-Pool-12", "application-insights", "Sandpit", this)

        assert filesGot.contains("configuration/imports/application_insights/templates/application-insights.json")
        assert filesGot.contains("configuration/imports/application_insights/parameters/sandpit/application-insights.parameters.json")

        def deployCommandsRan = testCommandRan ".*az deployment group create.*"
        assert deployCommandsRan.size() == 1
        assert deployCommandsRan[0].contains("-g SNDIMPINFRGP001-Pool-12")
        assert deployCommandsRan[0].contains("--name insights-SNDIMPINFRGP001-Pool-12")
        assert deployCommandsRan[0].contains("--template-file configuration/imports/application_insights/templates/application-insights.json")
        assert deployCommandsRan[0].contains("--parameters configuration/imports/application_insights/parameters/sandpit/application-insights.parameters.json")
        assert deployCommandsRan[0].contains("--parameters \"envSuffix=12\"")

        assert !deployCommandsRan[0].contains(" --no-wait")
    }

    @Test
    public void testDeployAppServicePlanComponent() {
        AzureActions.deployAppServicePlanComponent("SNDIMPINFRGP001-Pool-12", "services-general", "Sandpit", this)

        assert filesGot.contains("configuration/imports/appserviceplans/templates/appserviceplan.json")
        assert filesGot.contains("configuration/imports/appserviceplans/parameters/sandpit/services-general.parameters.json")

        def deployCommandsRan = testCommandRan ".*az deployment group create.*"
        assert deployCommandsRan.size() == 1
        assert deployCommandsRan[0].contains("-g SNDIMPINFRGP001-Pool-12")
        assert deployCommandsRan[0].contains("--name services-general")
        assert deployCommandsRan[0].contains("--template-file configuration/imports/appserviceplans/templates/appserviceplan.json")
        assert deployCommandsRan[0].contains("--parameters configuration/imports/appserviceplans/parameters/sandpit/services-general.parameters.json")
    }

    @Test
    public void testDeleteResourceGroup() {
        AzureActions.deleteResourceGroup("SNDIMPINFRGP001-Pool-12", true, this)

        def deployCommandsRan = testCommandRan ".*az group delete.*"
        assert deployCommandsRan.size() == 1
        assert deployCommandsRan[0].contains("--name SNDIMPINFRGP001-Pool-12")
        assert deployCommandsRan[0].contains("--yes")
    }

}
