package defra.pipeline.config

import defra.pipeline.BaseTest

import org.junit.Test

class ConfigTest extends BaseTest {

    @Test
    public void testGetPropertyValue() {
        def result = Config.getPropertyValue("projectName", this)
        assert result == "Imports"
    }

    @Test
    public void testGetPropertyValueNoneExistent() {
        def result = Config.getPropertyValue("aNameThatSurelyWillNeverExist", this)
        assert result == null
    }

    @Test
    public void testGetParameters() {
        def result = Config.getParameters("frontend-control", "sandpit", this)
        assert result.parameters.serviceName.value == "frontend-control"
    }

}
