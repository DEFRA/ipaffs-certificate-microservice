package defra.pipeline.names

import defra.pipeline.BaseTest

import org.junit.Test

class DockerNameTest extends BaseTest {

    @Test
    public void testGetNameAndTagForBranchAndSandpit() {
        def result = DockerName.getNameAndTag("feature/IMTA-1234-test-branch", "test-microservice", "Sandpit", "1.0", false, this)
        assert result == "sndeuxfesacr001.azurecr.io/imports-imta-1234/test-microservice:1.0"
    }

    @Test
    public void testGetNameAndTagForBadlyNamedBranchAndSandpit() {
        def result = DockerName.getNameAndTag("badly-named-branch", "test-microservice", "Sandpit", "1.0", false, this)
        assert result == "sndeuxfesacr001.azurecr.io/imports-badly-named-branch/test-microservice:1.0"
    }

    @Test
    public void testGetNameAndTagForMasterAndSandpit() {
        def result = DockerName.getNameAndTag("master", "test-microservice", "Sandpit", "1.0", false, this)
        assert result == "sndeuxfesacr001.azurecr.io/imports-master/test-microservice:1.0"
    }

    @Test
    public void testGetNameAndTagForMasterAndRelease() {
        def result = DockerName.getNameAndTag("master", "test-microservice", "Sandpit", "1.0", true, this)
        assert result == "sndeuxfesacr001.azurecr.io/imports-release/test-microservice:1.0"
    }

    @Test
    public void testGetConfigurationNameAndTagForBranchAndSandpit() {
        def result = DockerName.getConfigurationNameAndTag("feature/IMTA-1234-test-branch", "test-microservice", "Sandpit", "1.0", false, this)
        assert result == "sndeuxfesacr001.azurecr.io/imports-imta-1234/test-microservice-configuration:1.0"
    }

    @Test
    public void testGetConfigurationNameAndTagForMasterAndSandpit() {
        def result = DockerName.getConfigurationNameAndTag("master", "test-microservice", "Sandpit", "1.0", false, this)
        assert result == "sndeuxfesacr001.azurecr.io/imports-master/test-microservice-configuration:1.0"
    }

    @Test
    public void testGetConfigurationNameAndTagForMasterAndRelease() {
        def result = DockerName.getConfigurationNameAndTag("master", "test-microservice", "Sandpit", "1.0", true, this)
        assert result == "sndeuxfesacr001.azurecr.io/imports-release/test-microservice-configuration:1.0"
    }

    @Test
    public void testGetNameAndTagForHotfix() {
        def result = DockerName.getNameAndTag("hotfix/IMTA-1234-testing", "approvedestablishment-microservice", "Sandpit", "1.0.4", false, this)
        assert result == "sndeuxfesacr001.azurecr.io/imports-imta-1234/approvedestablishment-microservice:1.0.4"
    }

    @Test
    public void testGetNameAndTagForHotfixRelease() {
        def result = DockerName.getNameAndTag("hotfix/IMTA-1234-testing", "approvedestablishment-microservice", "Sandpit", "1.0.4", true, this)
        assert result == "sndeuxfesacr001.azurecr.io/imports-hotfix/approvedestablishment-microservice:1.0.4"
    }

}
