package defra.pipeline.environments

import defra.pipeline.BaseTest
import defra.pipeline.config.Config
import org.junit.Test

class EnvironmentActionsTest extends BaseTest {

    @Test
    public void testTagResourceGroup() {
        EnvironmentActions.tagResourceGroup("SNDIMPINFRGP001-Pool-12", "test-service", "test-tag", this)

        def updateCommandsRan = testCommandRan "az group update.*"
        assert updateCommandsRan.size() == 1
        assert updateCommandsRan[0].matches(".* --name SNDIMPINFRGP001-Pool-12.*")
        assert updateCommandsRan[0].matches(".* --set tags.BuildPool=\"test-service:test-tag\"")
    }

    @Test
    public void testReservePoolWhenFreePoolExists() {
        shellCommandsReturn["az group list.* --tag *BuildPool=Imports.*"] = "SNDIMPINFRGP001-Pool-4\nSNDIMPINFRGP001-Pool-5"
        def ret = EnvironmentActions.reserveFreePool("test-service", "test-tag", this)
        assert ret == "SNDIMPINFRGP001-Pool-4"

        def updateCommandsRan = testCommandRan "az group update.*"
        assert updateCommandsRan.size() == 1
        assert updateCommandsRan[0].matches(".* --name SNDIMPINFRGP001-Pool-4.*") || updateCommandsRan[0].matches(".* --name SNDIMPINFRGP001-Pool-5.*")
        assert updateCommandsRan[0].matches(".* --set tags.BuildPool=\"test-service:test-tag\"")
    }

    @Test
    public void testDoesntReservePoolWhenNoFreePoolExists() {
        shellCommandsReturn["az group list.* --tag *BuildPool=Imports.*"] = ""
        def ret = EnvironmentActions.reserveFreePool("test-service", "test-tag", this)
        assert ret == null

        def updateCommandsRan = testCommandRan "az group update.*"
        assert updateCommandsRan.size() == 0
    }

    @Test
    public void testRetryForFreePoolAndFail() {
        shellCommandsReturn["az group list.* --tag *BuildPool=Imports.*"] = ""

        def exceptionThrown = false
        try {
            EnvironmentActions.reserveFreePool("test-service", "test-tag", 3, 1, this)
        } catch (EnvironmentNoPoolsFreeException e) {
            exceptionThrown = true
        }
        assert exceptionThrown

        def getFreePoolsCommandsRan = testCommandRan "az group list.* --tag *BuildPool=Imports.*"
        assert getFreePoolsCommandsRan.size() == 3
    }

    @Test
    public void testCreateResourceGroup() {
        def ret = EnvironmentActions.createResourceGroup("SNDIMPINFRGP001-Pool-12", this)
        assert ret == true

        def createCommandsRan = testCommandRan "az group create.*"
        assert createCommandsRan.size() == 1
        assert createCommandsRan[0].matches(".* -n SNDIMPINFRGP001-Pool-12.*")

        def assignRolesCommandsRan = testCommandRan "az role assignment create.*"
        assert assignRolesCommandsRan.size() > 0

        def assignTagsCommandsRan = testCommandRan "az group update.*"
        assert assignTagsCommandsRan.size() == 1
    }

    @Test
    public void testCreatePool() {
        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-3\nSNDIMPINFRGP001-Pool-4"
        def ret = EnvironmentActions.createPool(this)
        assert ret == "SNDIMPINFRGP001-Pool-2"

        def createCommandsRan = testCommandRan "az group create.*"
        assert createCommandsRan.size() == 1
        assert createCommandsRan[0].matches(".* -n SNDIMPINFRGP001-Pool-2.*")

        def assignRolesCommandsRan = testCommandRan "az role assignment create.*"
        assert assignRolesCommandsRan.size() > 0

        def assignTagsCommandsRan = testCommandRan "az group update.*"
        assert assignTagsCommandsRan.size() == 1
    }

    @Test
    public void testCreatePoolFailsWhenMaxPoolsHit() {
        def maxNumPoolsLimit = Config.getPropertyValue("maxNumPoolsLimit", this).toInteger()
        def poolList = []
        for (i in 1..maxNumPoolsLimit) {
            poolList.add("SNDIMPINFRGP001-Pool-${i}")
        }

        shellCommandsReturn["az group list.*"] = poolList.join('\n')

        def thrownException = false

        try {
            EnvironmentActions.createPool(this)
        } catch (EnvironmentMaxPoolsLimitException e) {
            thrownException = true
        }

        def createCommandsRan = testCommandRan "az group create.*"
        assert createCommandsRan.size() == 0
        assert thrownException
    }
}

