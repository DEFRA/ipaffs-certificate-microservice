package defra.pipeline.database

import defra.pipeline.BaseTest

import org.junit.Test

class DatabaseActionsTest extends BaseTest {

    @Test
    public void testCreateDatabaseWhenNotExists() {
        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"
        shellCommandsReturn["az sql db list.*"] = "test-microservice-db\ntest2-microservice-db"
        def result = DatabaseActions.createDatabase("notification-microservice", "SNDIMPINFRGP001-Pool-3", this)
        assert result

        def createCommandsRan = testCommandRan "az sql db create.*"
        assert createCommandsRan.size() == 1
        assert createCommandsRan[0].matches(".* -n notification-microservice-3.*")
    }

    @Test
    public void testCreateEconomicOperatorDatabaseWhenNotExists() {
        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"
        shellCommandsReturn["az sql db list.*"] = "test-microservice-db\ntest2-microservice-db"
        def result = DatabaseActions.createDatabase("economicoperator-microservice", "SNDIMPINFRGP001-Pool-3", this)
        assert result

        def createCommandsRan = testCommandRan "az sql db create.*"
        assert createCommandsRan.size() == 2
        assert createCommandsRan[0].matches(".* -n economicoperator-microservice-3.*")
        assert createCommandsRan[1].matches(".* -n economicoperator-microservice-public-3.*")
    }

    @Test
    public void testNotCreateDatabaseWhenExists() {
        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"
        shellCommandsReturn["az sql db list.*"] = "notification-microservice-3\ntest-microservice-db\ntest2-microservice-db"
        def result = DatabaseActions.createDatabase("notification-microservice", "SNDIMPINFRGP001-Pool-3", this)
        assert !result

        def createCommandFound = testCommandRan "az sql db create.*"
        assert createCommandFound.size() == 0
    }

    @Test
    public void testNotCreateEconomicOperatorDatabaseWhenExists() {
        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"
        shellCommandsReturn["az sql db list.*"] = "economicoperator-microservice-3\neconomicoperator-microservice-public-3\ntest2-microservice-db"
        def result = DatabaseActions.createDatabase("economicoperator-microservice", "SNDIMPINFRGP001-Pool-3", this)
        assert !result

        def createCommandFound = testCommandRan "az sql db create.*"
        assert createCommandFound.size() == 0
    }

    @Test
    public void testDropDatabaseWhenExistsInPool() {

        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"
        shellCommandsReturn["az sql db list.*"] = "notification-microservice-3\ntest-microservice-db\ntest2-microservice-db"
        def result = DatabaseActions.dropDatabase("notification-microservice", "SNDIMPINFRGP001-Pool-3", this)
        assert result

        def dropDatabaseCommandFound = testCommandRan "az sql db delete.*"
        assert dropDatabaseCommandFound.size() == 1
        assert dropDatabaseCommandFound[0].matches(".* --name notification-microservice-3.*")
    }

    @Test
    public void testDropEconomicOperatorDatabaseWhenExistsInPool() {

        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"
        shellCommandsReturn["az sql db list.*"] = "economicoperator-microservice-3\neconomicoperator-microservice-public-3\ntest2-microservice-db"
        def result = DatabaseActions.dropDatabase("economicoperator-microservice", "SNDIMPINFRGP001-Pool-3", this)
        assert result

        def dropDatabaseCommandFound = testCommandRan "az sql db delete.*"
        assert dropDatabaseCommandFound.size() == 2
        assert dropDatabaseCommandFound[0].matches(".* --name economicoperator-microservice-3.*")
        assert dropDatabaseCommandFound[1].matches(".* --name economicoperator-microservice-public-3.*")
    }

    @Test
    public void testNotDropDatabaseWhenNotExists() {

        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"
        shellCommandsReturn["az sql db list.*"] = "test-microservice-db\ntest2-microservice-db"
        def result = DatabaseActions.dropDatabase("notification-microservice", "SNDIMPINFRGP001-Pool-3", this)
        assert !result

        def dropDatabaseCommandFound = testCommandRan "az sql db delete.*"
        assert dropDatabaseCommandFound.size() == 0
    }

    @Test
    public void testNotDropEconomicOperatorDatabaseWhenNotExists() {

        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"
        shellCommandsReturn["az sql db list.*"] = "test-microservice-db\ntest2-microservice-db"
        def result = DatabaseActions.dropDatabase("economicoperator-microservice", "SNDIMPINFRGP001-Pool-3", this)
        assert !result

        def dropDatabaseCommandFound = testCommandRan "az sql db delete.*"
        assert dropDatabaseCommandFound.size() == 0
    }

    @Test
    public void testNotDropDatabaseWhenNotAPool() {

        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"

        def exceptionThrown = false
        try {
            def result = DatabaseActions.dropDatabase("notification-microservice", "SNDIMPINFRGP001-imports-static-test", this)
        } catch (DatabaseAlteringNonPoolException e) {
            exceptionThrown = true
        }

        assert exceptionThrown

    }

    @Test
    public void testDropAllForeignKeys() {

        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"
        shellCommandsReturn["az sql db list.*"] = "notification-microservice-3\ntest-microservice-db\ntest2-microservice-db"
        shellCommandsReturn["az keyvault secret show.*"] = "abcd1234"
        shellCommandsReturn[".*/opt/mssql-tools/bin/sqlcmd .*SELECT .*name.* FROM sys.foreign_keys.*"] = """fk_organisation_type_organisation_id organisation_type
fk_organisation_type_type_id organisation_type
"""
        def result = DatabaseActions.dropAllForeignKeys("notification-microservice", "SNDIMPINFRGP001-Pool-3", this)

        assert result

        def dropCommandsRan = testCommandRan ".*/opt/mssql-tools/bin/sqlcmd .*ALTER TABLE .* DROP CONSTRAINT .*"
        assert dropCommandsRan.size() == 2
        assert dropCommandsRan[0].contains("organisation_type")
        assert dropCommandsRan[0].contains("fk_organisation_type_organisation_id")
        assert dropCommandsRan[1].contains("organisation_type")
        assert dropCommandsRan[1].contains("fk_organisation_type_type_id")

    }

    @Test
    public void testNotDropAllForeignKeysWhenNoDBExists() {

        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"
        shellCommandsReturn["az sql db list.*"] = "test-microservice-db\ntest2-microservice-db"
        def result = DatabaseActions.dropAllForeignKeys("notification-microservice", "SNDIMPINFRGP001-Pool-3", this)
        assert !result

    }

    @Test
    public void testNotDropAllForeignKeysWhenNotAPool() {

        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"

        def exceptionThrown = false
        try {
            def result = DatabaseActions.dropAllForeignKeys("notification-microservice", "SNDIMPINFRGP001-imports-static-test", this)
        } catch (DatabaseAlteringNonPoolException e) {
            exceptionThrown = true
        }

        assert exceptionThrown

    }

    @Test
    public void testDropAllForeignKeysEconomicOperator() {

        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"
        shellCommandsReturn["az sql db list.*"] = "economicoperator-microservice-3\neconomicoperator-microservice-public-3\ntest2-microservice-db"
        shellCommandsReturn["az keyvault secret show.*"] = "abcd1234"
        shellCommandsReturn[".*/opt/mssql-tools/bin/sqlcmd .*SELECT .*name.* FROM sys.foreign_keys.*"] = """fk_organisation_type_organisation_id organisation_type
fk_organisation_type_type_id organisation_type
"""
        def result = DatabaseActions.dropAllForeignKeysEconomicOperator("SNDIMPINFRGP001-Pool-3", this)

        assert result

        def dropCommandsRan = testCommandRan ".*/opt/mssql-tools/bin/sqlcmd .*ALTER TABLE .* DROP CONSTRAINT .*"
        assert dropCommandsRan.size() == 4
        assert dropCommandsRan[0].contains("organisation_type")
        assert dropCommandsRan[0].contains("fk_organisation_type_organisation_id")
        assert dropCommandsRan[1].contains("organisation_type")
        assert dropCommandsRan[1].contains("fk_organisation_type_type_id")
        assert dropCommandsRan[2].contains("organisation_type")
        assert dropCommandsRan[2].contains("fk_organisation_type_organisation_id")
        assert dropCommandsRan[3].contains("organisation_type")
        assert dropCommandsRan[3].contains("fk_organisation_type_type_id")

    }

    @Test
    public void testNotDropAllForeignKeysEconomicOperatorWhenNoDBExists() {

        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"
        shellCommandsReturn["az sql db list.*"] = "test-microservice-db\ntest2-microservice-db"
        def result = DatabaseActions.dropAllForeignKeysEconomicOperator("SNDIMPINFRGP001-Pool-3", this)
        assert !result

    }

    @Test
    public void testNotDropAllForeignKeysEconomicOperatorWhenNotAPool() {

        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"

        def exceptionThrown = false
        try {
            def result = DatabaseActions.dropAllForeignKeysEconomicOperator("SNDIMPINFRGP001-imports-static-test", this)
        } catch (DatabaseAlteringNonPoolException e) {
            exceptionThrown = true
        }

        assert exceptionThrown

    }

    @Test
    public void testDropAllTables() {

        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"
        shellCommandsReturn["az sql db list.*"] = "notification-microservice-3\ntest-microservice-db\ntest2-microservice-db"
        shellCommandsReturn["az keyvault secret show.*"] = "abcd1234"
        shellCommandsReturn[".*/opt/mssql-tools/bin/sqlcmd .*SELECT .*name.* FROM sys.tables.*"] = """dbo.commodity_categories
dbo.commodity_code
dbo.DATABASECHANGELOG
dbo.DATABASECHANGELOGLOCK
dbo.field_configuration
dbo.veterinary_establishment
"""
        def result = DatabaseActions.dropAllTables("notification-microservice", "SNDIMPINFRGP001-Pool-3", this)

        assert result

        def dropCommandsRan = testCommandRan ".*/opt/mssql-tools/bin/sqlcmd .*DROP TABLE .*"
        assert dropCommandsRan.size() == 6
        assert dropCommandsRan[0].contains("dbo.commodity_categories")
        assert dropCommandsRan[1].contains("dbo.commodity_code")
        assert dropCommandsRan[2].contains("dbo.DATABASECHANGELOG")
        assert dropCommandsRan[3].contains("dbo.DATABASECHANGELOGLOCK")
        assert dropCommandsRan[4].contains("dbo.field_configuration")
        assert dropCommandsRan[5].contains("dbo.veterinary_establishment")

    }

    @Test
    public void testNotDropAllTablesWhenNoDBExists() {

        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"
        shellCommandsReturn["az sql db list.*"] = "test-microservice-db\ntest2-microservice-db"
        def result = DatabaseActions.dropAllTables("notification-microservice", "SNDIMPINFRGP001-Pool-3", this)
        assert !result

    }

    @Test
    public void testNotDropAllTablesWhenNotAPool() {

        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"

        def exceptionThrown = false
        try {
            def result = DatabaseActions.dropAllTables("notification-microservice", "SNDIMPINFRGP001-imports-static-test", this)
        } catch (DatabaseAlteringNonPoolException e) {
            exceptionThrown = true
        }

        assert exceptionThrown

    }

    @Test
    public void testDropAllTablesEconomicOperator() {

        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"
        shellCommandsReturn["az sql db list.*"] = "economicoperator-microservice-3\neconomicoperator-microservice-public-3\ntest2-microservice-db"
        shellCommandsReturn["az keyvault secret show.*"] = "abcd1234"
        shellCommandsReturn[".*/opt/mssql-tools/bin/sqlcmd .*SELECT .*name.* FROM sys.tables.*"] = """dbo.commodity_categories
dbo.commodity_code
dbo.DATABASECHANGELOG
dbo.DATABASECHANGELOGLOCK
dbo.field_configuration
dbo.veterinary_establishment
"""
        def result = DatabaseActions.dropAllTablesEconomicOperator("SNDIMPINFRGP001-Pool-3", this)

        assert result

        def dropCommandsRan = testCommandRan ".*/opt/mssql-tools/bin/sqlcmd .*DROP TABLE .*"
        assert dropCommandsRan.size() == 12
        assert dropCommandsRan[0].contains("dbo.commodity_categories")
        assert dropCommandsRan[1].contains("dbo.commodity_code")
        assert dropCommandsRan[2].contains("dbo.DATABASECHANGELOG")
        assert dropCommandsRan[3].contains("dbo.DATABASECHANGELOGLOCK")
        assert dropCommandsRan[4].contains("dbo.field_configuration")
        assert dropCommandsRan[5].contains("dbo.veterinary_establishment")
        assert dropCommandsRan[6].contains("dbo.commodity_categories")
        assert dropCommandsRan[7].contains("dbo.commodity_code")
        assert dropCommandsRan[8].contains("dbo.DATABASECHANGELOG")
        assert dropCommandsRan[9].contains("dbo.DATABASECHANGELOGLOCK")
        assert dropCommandsRan[10].contains("dbo.field_configuration")
        assert dropCommandsRan[11].contains("dbo.veterinary_establishment")

    }

    @Test
    public void testNotDropAllTablesEconomicOperatorWhenNoDBExists() {

        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"
        shellCommandsReturn["az sql db list.*"] = "test-microservice-db\ntest2-microservice-db"
        def result = DatabaseActions.dropAllTablesEconomicOperator("SNDIMPINFRGP001-Pool-3", this)
        assert !result

    }

    @Test
    public void testNotDropAllTablesEconomicOperatorWhenNotAPool() {

        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"

        def exceptionThrown = false
        try {
            def result = DatabaseActions.dropAllTablesEconomicOperator("SNDIMPINFRGP001-imports-static-test", this)
        } catch (DatabaseAlteringNonPoolException e) {
            exceptionThrown = true
        }

        assert exceptionThrown

    }

    @Test
    public void testDropAllUsers() {

        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"
        shellCommandsReturn["az sql db list.*"] = "notification-microservice-3\ntest-microservice-db\ntest2-microservice-db"
        shellCommandsReturn["az keyvault secret show.*"] = "abcd1234"
        shellCommandsReturn[".*/opt/mssql-tools/bin/sqlcmd .*SELECT .*name.* FROM sysusers.*"] = """dbo
notificationServiceUser
ms_notification_azure
"""
        def result = DatabaseActions.dropAllUsers("notification-microservice", "SNDIMPINFRGP001-Pool-3", this)

        assert result

        def dropCommandsRan = testCommandRan ".*/opt/mssql-tools/bin/sqlcmd .*DROP USER .*"
        assert dropCommandsRan.size() == 2
        assert dropCommandsRan[0].contains("notificationServiceUser")
        assert dropCommandsRan[1].contains("ms_notification_azure")

    }

    @Test
    public void testNotDropAllUsersWhenNoDBExists() {

        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"
        shellCommandsReturn["az sql db list.*"] = "test-microservice-db\ntest2-microservice-db"
        def result = DatabaseActions.dropAllUsers("notification-microservice", "SNDIMPINFRGP001-Pool-3", this)
        assert !result

    }

    @Test
    public void testNotDropAllUserWhenNotAPool() {

        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"

        def exceptionThrown = false
        try {
            def result = DatabaseActions.dropAllUsers("notification-microservice", "SNDIMPINFRGP001-imports-static-test", this)
        } catch (DatabaseAlteringNonPoolException e) {
            exceptionThrown = true
        }

        assert exceptionThrown

    }

    @Test
    public void testDropAllUsersEconomicOperator() {

        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"
        shellCommandsReturn["az sql db list.*"] = "economicoperator-microservice-3\neconomicoperator-microservice-public-3\ntest2-microservice-db"
        shellCommandsReturn["az keyvault secret show.*"] = "abcd1234"
        shellCommandsReturn[".*/opt/mssql-tools/bin/sqlcmd .*SELECT .*name.* FROM sysusers.*"] = """dbo
notificationServiceUser
ms_notification_azure
"""
        def result = DatabaseActions.dropAllUsersEconomicOperator("SNDIMPINFRGP001-Pool-3", this)

        assert result

        def dropCommandsRan = testCommandRan ".*/opt/mssql-tools/bin/sqlcmd .*DROP USER .*"
        assert dropCommandsRan.size() == 4
        assert dropCommandsRan[0].contains("notificationServiceUser")
        assert dropCommandsRan[1].contains("ms_notification_azure")
        assert dropCommandsRan[2].contains("notificationServiceUser")
        assert dropCommandsRan[3].contains("ms_notification_azure")

    }

    @Test
    public void testNotDropAllUsersEconomicOperatorWhenNoDBExists() {

        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"
        shellCommandsReturn["az sql db list.*"] = "test-microservice-db\ntest2-microservice-db"
        def result = DatabaseActions.dropAllUsersEconomicOperator("SNDIMPINFRGP001-Pool-3", this)
        assert !result

    }

    @Test
    public void testNotDropAllUsersEconomicOperatorWhenNotAPool() {

        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"

        def exceptionThrown = false
        try {
            def result = DatabaseActions.dropAllUsersEconomicOperator("SNDIMPINFRGP001-imports-static-test", this)
        } catch (DatabaseAlteringNonPoolException e) {
            exceptionThrown = true
        }

        assert exceptionThrown

    }

    @Test
    public void testDropAllViews() {

        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"
        shellCommandsReturn["az sql db list.*"] = "notification-microservice-3\ntest-microservice-db\ntest2-microservice-db"
        shellCommandsReturn["az keyvault secret show.*"] = "abcd1234"
        shellCommandsReturn[".*/opt/mssql-tools/bin/sqlcmd .*SELECT .*name.* FROM sys.views.*"] = "v_azure_search\n"
        def result = DatabaseActions.dropAllViews("notification-microservice", "SNDIMPINFRGP001-Pool-3", this)

        assert result

        def dropCommandsRan = testCommandRan ".*/opt/mssql-tools/bin/sqlcmd .*DROP VIEW .*"
        assert dropCommandsRan.size() == 1
        assert dropCommandsRan[0].contains("v_azure_search")

    }

    @Test
    public void testNotDropAllViewsWhenNoDBExists() {

        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"
        shellCommandsReturn["az sql db list.*"] = "test-microservice-db\ntest2-microservice-db"
        def result = DatabaseActions.dropAllViews("notification-microservice", "SNDIMPINFRGP001-Pool-3", this)
        assert !result

    }

    @Test
    public void testNotDropAllViewsWhenNotAPool() {

        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"

        def exceptionThrown = false
        try {
            def result = DatabaseActions.dropAllViews("notification-microservice", "SNDIMPINFRGP001-imports-static-test", this)
        } catch (DatabaseAlteringNonPoolException e) {
            exceptionThrown = true
        }

        assert exceptionThrown

    }

    @Test
    public void testDropAllViewsEconomicOperator() {

        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"
        shellCommandsReturn["az sql db list.*"] = "economicoperator-microservice-3\neconomicoperator-microservice-public-3\ntest2-microservice-db"
        shellCommandsReturn["az keyvault secret show.*"] = "abcd1234"
        shellCommandsReturn[".*/opt/mssql-tools/bin/sqlcmd .*SELECT .*name.* FROM sys.views.*"] = "v_azure_search\n"
        def result = DatabaseActions.dropAllViewsEconomicOperator("SNDIMPINFRGP001-Pool-3", this)

        assert result

        def dropCommandsRan = testCommandRan ".*/opt/mssql-tools/bin/sqlcmd .*DROP VIEW .*"
        assert dropCommandsRan.size() == 2
        assert dropCommandsRan[0].contains("v_azure_search")
        assert dropCommandsRan[1].contains("v_azure_search")

    }

    @Test
    public void testNotDropAllViewsEconomicOperatorWhenNoDBExists() {

        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"
        shellCommandsReturn["az sql db list.*"] = "test-microservice-db\ntest2-microservice-db"
        def result = DatabaseActions.dropAllViewsEconomicOperator("SNDIMPINFRGP001-Pool-3", this)
        assert !result

    }

    @Test
    public void testNotDropAllViewsEconomicOperatorWhenNotAPool() {

        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"

        def exceptionThrown = false
        try {
            def result = DatabaseActions.dropAllViewsEconomicOperator("SNDIMPINFRGP001-imports-static-test", this)
        } catch (DatabaseAlteringNonPoolException e) {
            exceptionThrown = true
        }

        assert exceptionThrown

    }

    @Test
    public void testDropAllSequences() {

        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"
        shellCommandsReturn["az sql db list.*"] = "checks-microservice-3\ntest-microservice-db\ntest2-microservice-db"
        shellCommandsReturn["az keyvault secret show.*"] = "abcd1234"
        shellCommandsReturn[".*/opt/mssql-tools/bin/sqlcmd .*SELECT .*name.* FROM sys.sequences.*"] = "dbo.req_seq\n"
        def result = DatabaseActions.dropAllSequences("checks-microservice", "SNDIMPINFRGP001-Pool-3", this)

        assert result

        def dropCommandsRan = testCommandRan ".*/opt/mssql-tools/bin/sqlcmd .*DROP SEQUENCE .*"
        assert dropCommandsRan.size() == 1
        assert dropCommandsRan[0].contains("dbo.req_seq")

    }

    @Test
    public void testNotDropAllSequencesWhenNoDBExists() {

        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"
        shellCommandsReturn["az sql db list.*"] = "test-microservice-db\ntest2-microservice-db"
        def result = DatabaseActions.dropAllSequences("checks-microservice", "SNDIMPINFRGP001-Pool-3", this)
        assert !result

    }

    @Test
    public void testNotDropAllSequencesWhenNotAPool() {

        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"

        def exceptionThrown = false
        try {
            def result = DatabaseActions.dropAllSequences("checks-microservice", "SNDIMPINFRGP001-imports-static-test", this)
        } catch (DatabaseAlteringNonPoolException e) {
            exceptionThrown = true
        }

        assert exceptionThrown

    }

    @Test
    public void testDropAllSequencesEconomicOperator() {

        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"
        shellCommandsReturn["az sql db list.*"] = "economicoperator-microservice-3\neconomicoperator-microservice-public-3\ntest2-microservice-db"
        shellCommandsReturn["az keyvault secret show.*"] = "abcd1234"
        shellCommandsReturn[".*/opt/mssql-tools/bin/sqlcmd .*SELECT .*name.* FROM sys.sequences.*"] = "dbo.req_seq\n"
        def result = DatabaseActions.dropAllSequencesEconomicOperator( "SNDIMPINFRGP001-Pool-3", this)

        assert result

        def dropCommandsRan = testCommandRan ".*/opt/mssql-tools/bin/sqlcmd .*DROP SEQUENCE .*"
        assert dropCommandsRan.size() == 2
        assert dropCommandsRan[0].contains("dbo.req_seq")
        assert dropCommandsRan[1].contains("dbo.req_seq")

    }

    @Test
    public void testNotDropAllSequencesEconomicOperatorWhenNoDBExists() {

        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"
        shellCommandsReturn["az sql db list.*"] = "test-microservice-db\ntest2-microservice-db"
        def result = DatabaseActions.dropAllSequencesEconomicOperator("SNDIMPINFRGP001-Pool-3", this)
        assert !result

    }

    @Test
    public void testNotDropAllSequencesEconomicOperatorWhenNotAPool() {

        shellCommandsReturn["az group list.*"] = "SNDIMPINFRGP001-Pool-1\nSNDIMPINFRGP001-Pool-2\nSNDIMPINFRGP001-Pool-3"

        def exceptionThrown = false
        try {
            def result = DatabaseActions.dropAllSequencesEconomicOperator("SNDIMPINFRGP001-imports-static-test", this)
        } catch (DatabaseAlteringNonPoolException e) {
            exceptionThrown = true
        }

        assert exceptionThrown

    }

}
