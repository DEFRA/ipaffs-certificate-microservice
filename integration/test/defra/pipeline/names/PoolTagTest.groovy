package defra.pipeline.names

import defra.pipeline.BaseTest

import org.junit.Test

class PoolTagTest extends BaseTest {

    @Test
    public void getNameDoesNotAppendResourceGroupNameIfWouldResultInDuplicatedSuffix() {
        def result = PoolTag.getNameWithTag("service-name-feature", "SNDIMPINFRGP001-Pool-feature")
        assert result == "service-name-feature"
    }
    
    @Test
    public void testGetPoolId() {
        def result = PoolTag.getId("SNDIMPINFRGP001-Pool-12")
        assert result == "12"
    }

    @Test
    public void testGetPoolIdForTest() {
        def result = PoolTag.getId("SNDIMPINFRGP001-imports-static-test")
        assert result == "test"
    }

    @Test
    public void testGetPoolName() {
        def result = PoolTag.getNameWithTag("frontend-notification", "SNDIMPINFRGP001-Pool-12")
        assert result == "frontend-notification-12"
    }

}
