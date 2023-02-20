package defra.pipeline.environments

import defra.pipeline.BaseTest
import org.junit.Test

class EnvironmentQueriesTest extends BaseTest {

    @Test
    public void testGetAllEnvironments() {
        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3\nSNDIMPINFRGP001-imports-static-test"
        def result = EnvironmentQueries.getAllResourceGroups(this)

        def groupListCommandFound = testCommandRan "az group list.*"
        assert groupListCommandFound.size() == 1
        assert groupListCommandFound[0].matches(".* -o tsv.*")
        assert result == ['SNDIMPINFRGP001-Pool-1',
                          'SNDIMPINFRGP001-Pool-2',
                          'SNDIMPINFRGP001-Pool-3',
                          'SNDIMPINFRGP001-imports-static-test']
    }

    @Test
    public void testGetAllPools() {
        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"
        def result = EnvironmentQueries.getAllPools(this)

        def groupListCommandFound = testCommandRan "az group list.*"
        assert groupListCommandFound.size() == 1
        assert groupListCommandFound[0].matches(".* --query \"\\[\\?contains\\(name, 'SNDIMPINFRGP001-Pool-'\\)\\]\\.name\".*")
        assert groupListCommandFound[0].matches(".* -o tsv.*")
        assert result == ['SNDIMPINFRGP001-Pool-1',
                          'SNDIMPINFRGP001-Pool-2',
                          'SNDIMPINFRGP001-Pool-3']
    }

    @Test
    public void testGetAllHotfixPools() {
        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Hotfix-Pool-850\nSNDIMPINFRGP001-Hotfix-Pool-860\nSNDIMPINFRGP001-Hotfix-Pool-870"
        def result = EnvironmentQueries.getAllHotfixPools(this)

        def groupListCommandFound = testCommandRan "az group list.*"
        assert groupListCommandFound.size() == 1
        assert groupListCommandFound[0].matches(".* --query \"\\[\\?contains\\(name, 'SNDIMPINFRGP001-Hotfix-Pool-'\\)\\]\\.name\".*")
        assert groupListCommandFound[0].matches(".* -o tsv.*")
        assert result == ['SNDIMPINFRGP001-Hotfix-Pool-850',
                          'SNDIMPINFRGP001-Hotfix-Pool-860',
                          'SNDIMPINFRGP001-Hotfix-Pool-870']
    }

    @Test
    public void testGetFreePools() {
        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-3"
        def result = EnvironmentQueries.getFreePools(this)

        def groupListCommandFound = testCommandRan "az group list.*"
        assert groupListCommandFound.size() == 1
        assert groupListCommandFound[0].matches(".* --query \"\\[\\?contains\\(name, 'SNDIMPINFRGP001-Pool-'\\)\\]\\.name\".*")
        assert groupListCommandFound[0].matches(".* -o tsv.*")
        assert groupListCommandFound[0].matches(".* --tag BuildPool=Imports.*")
        assert result == ['SNDIMPINFRGP001-Pool-1',
                          'SNDIMPINFRGP001-Pool-3']
    }

    @Test
    public void testGetNextIDForPool() {
        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"
        def result = EnvironmentQueries.getNextIdForPoolCreation(this)
        assert result == 4
    }

    @Test
    public void testGetNextIDForPoolWhenOneMissing() {
        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-3"
        def result = EnvironmentQueries.getNextIdForPoolCreation(this)
        assert result == 2
    }

    @Test
    public void testExistingPoolWithTagReturnsAResult() {
        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-4"
        def ret = EnvironmentQueries.existingPoolWithTag("test-service", "test-tag", this)
        assert ret == "SNDIMPINFRGP001-Pool-4"
    }

    @Test
    public void testExistingPoolWithTagReturnsNoResults() {
        shellCommandsReturn["az group list.*"] = ""
        def ret = EnvironmentQueries.existingPoolWithTag("test-service", "test-tag", this)
        assert ret == null
    }
}
