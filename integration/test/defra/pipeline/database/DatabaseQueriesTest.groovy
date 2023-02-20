package defra.pipeline.database

import defra.pipeline.BaseTest

import org.junit.Test

class DatabaseQueriesTest extends BaseTest {

    @Test
    public void testCheckDatabaseExists() {
        shellCommandsReturn["az sql db list.*"] = "test-microservice-db\ntest2-microservice-db"
        def result = DatabaseQueries.databaseExists("test-microservice-db", false, this)
        assert result == true
    }

    @Test
    public void testCheckDatabaseNotExists() {
        shellCommandsReturn["az sql db list.*"] = "another-microservice-db\ntest2-microservice-db"
        def result = DatabaseQueries.databaseExists("test-microservice-db", false, this)
        assert result == false
    }

}
