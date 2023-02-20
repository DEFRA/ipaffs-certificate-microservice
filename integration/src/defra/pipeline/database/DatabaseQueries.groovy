package defra.pipeline.database

import defra.pipeline.config.Config
import defra.pipeline.azure.AzureQueries
import defra.pipeline.script.ScriptActions
import defra.pipeline.vault.VaultKey

class DatabaseQueries {

    /**
     * Check that a database exists
     *
     * @param databaseName  The name of the database
     * @param script        The global script parameter
     */
    public static boolean databaseExists(String databaseName, boolean cache, Script script) {

        def databaseResourceGroup = Config.getPropertyValue("databaseResourceGroup", script)
        def databaseServer = Config.getPropertyValue("azureDatabaseServer", script)

        def azQuery = "az sql db list --resource-group ${databaseResourceGroup} --server ${databaseServer}  --query [].name --output tsv"
        def dbList
        if (cache) {
            dbList = AzureQueries.runCachedQueryForAzCli(azQuery, false, script)
        } else {
            dbList = script.sh(script: azQuery, returnStdout: true)
        }
        def listOfDatabases = dbList.split() as List

        def dbExists = listOfDatabases.find { it == databaseName }
        return dbExists
    }

    /**
     * Creates Azure SQL database
     * @param databaseName          The name of the database to create
     * @param script                The global script parameter
     * @return                      True if database created, false if not
     */
    public static boolean createDatabase(String databaseName, Script script) {

        if (!databaseExists(databaseName, false, script)) {
            def databaseResourceGroup = Config.getPropertyValue("databaseResourceGroup", script)
            def databaseServer = Config.getPropertyValue("azureDatabaseServer", script)
            def elasticPool = Config.getPropertyValue("databaseElasticPool", script)

            def scriptToRun = """ \
            az sql db create --resource-group ${databaseResourceGroup} --server ${databaseServer} -n ${databaseName} \
            --collation SQL_Latin1_General_CP1_CI_AS --elastic-pool ${elasticPool} \
            """.toString().trim()
            script.echo "Creating database ${databaseName} " + scriptToRun
            ScriptActions.runCommandLogOnlyOnError(scriptToRun, script)
            return true
        } else {
            script.echo "Not creating database ${databaseName} as it exists."
            return false
        }
    }

    /**
     * Drops all foreign keys
     * @param databaseName          The name of the database
     * @param script                The global script parameter
     * @return                      True if keys dropped, false if not
     */
    public static boolean dropAllForeignKeys(String databaseName, Script script) {

        if (databaseExists(databaseName, false, script)) {
            def databaseServer = Config.getPropertyValue("azureDatabaseServer", script)
            def databaseAdminUsername = Config.getPropertyValue("serviceAdminDBUsername", script)
            def databaseAdminPassword = VaultKey.getSecuredValue("DatabaseDBPassword", script)

            def allForeignKeysOutput = script.sh(script: "set +x;/opt/mssql-tools/bin/sqlcmd -d ${databaseName} -G -U ${databaseAdminUsername} -P ${databaseAdminPassword} -S 'tcp:${databaseServer}.database.windows.net,1433' -Q 'SET NOCOUNT ON; SELECT name, OBJECT_NAME(parent_object_id) FROM sys.foreign_keys' -C -l 30 -W -h-1", returnStdout: true)

            println(allForeignKeysOutput);

            for (data in allForeignKeysOutput.split('\n')) {
                if (data == "") {
                    continue
                }

                data = data.tokenize(' ')
                def fkName = data[0]
                def tableName = data[1]

                script.echo("Dropping Foreign Key ${fkName} on table ${tableName}")
                script.sh(script: "set +x;/opt/mssql-tools/bin/sqlcmd -d ${databaseName} -G -U ${databaseAdminUsername} -P ${databaseAdminPassword} -S 'tcp:${databaseServer}.database.windows.net,1433' -Q 'ALTER TABLE ${tableName} DROP CONSTRAINT ${fkName}' -C -l 30 -W", returnStdout: true)
            }
            return true
        } else {
            return false
        }
    }

    /**
     * Drop all tables
     * @param databaseName          The name of the database
     * @param script                The global script parameter
     * @return                      True if tables dropped, false if not
     */
    public static boolean dropAllTables(String databaseName, Script script) {
        if (databaseExists(databaseName, false, script)) {
            def databaseServer = Config.getPropertyValue("azureDatabaseServer", script)
            def databaseAdminUsername = Config.getPropertyValue("serviceAdminDBUsername", script)
            def databaseAdminPassword = VaultKey.getSecuredValue("DatabaseDBPassword", script)

            def allTablesOutput = script.sh(script: "set +x;/opt/mssql-tools/bin/sqlcmd -d ${databaseName} -G -U ${databaseAdminUsername} -P ${databaseAdminPassword} -S 'tcp:${databaseServer}.database.windows.net,1433' -Q 'SET NOCOUNT ON; SELECT SCHEMA_NAME(schema_id) + \".\" + name FROM sys.tables ORDER BY name' -C -l 30 -W -h-1", returnStdout: true)

            for (tableName in allTablesOutput.split('\n')) {
                if (tableName == "") {
                    continue
                }

                script.echo("Dropping Table: ${tableName}")
                script.sh(script: "set +x;/opt/mssql-tools/bin/sqlcmd -d ${databaseName} -G -U ${databaseAdminUsername} -P ${databaseAdminPassword} -S 'tcp:${databaseServer}.database.windows.net,1433' -Q 'DROP TABLE ${tableName}' -C -l 30 -W", returnStdout: true)
            }
            return true
        } else {
            return false
        }
    }

    /**
     * Drop all temporal tables
     * @param databaseName          The name of the database
     * @param script                The global script parameter
     * @return                      True if tables dropped, false if not
     */
    public static boolean dropAllTemporalTables(String databaseName, Script script) {
        if (databaseExists(databaseName, false, script)) {
            def databaseServer = Config.getPropertyValue("azureDatabaseServer", script)
            def databaseAdminUsername = Config.getPropertyValue("serviceAdminDBUsername", script)
            def databaseAdminPassword = VaultKey.getSecuredValue("DatabaseDBPassword", script)

            def allTablesOutput = script.sh(script: "set +x;/opt/mssql-tools/bin/sqlcmd -d ${databaseName} -G -U ${databaseAdminUsername} -P ${databaseAdminPassword} -S 'tcp:${databaseServer}.database.windows.net,1433' -Q 'SET NOCOUNT ON; SELECT name FROM sys.tables where temporal_type = 2' -C -l 30 -W -h-1", returnStdout: true)

            for (tableName in allTablesOutput.split('\n')) {
                if (tableName == "") {
                    continue
                }

                script.echo("Dropping temporal Table: ${tableName}")
                script.sh(script: "set +x;/opt/mssql-tools/bin/sqlcmd -d ${databaseName} -G -U ${databaseAdminUsername} -P ${databaseAdminPassword} -S 'tcp:${databaseServer}.database.windows.net,1433' -Q 'ALTER TABLE ${tableName} SET (SYSTEM_VERSIONING = OFF)' -C -l 30 -W", returnStdout: true)
                script.sh(script: "set +x;/opt/mssql-tools/bin/sqlcmd -d ${databaseName} -G -U ${databaseAdminUsername} -P ${databaseAdminPassword} -S 'tcp:${databaseServer}.database.windows.net,1433' -Q 'DROP TABLE ${tableName}' -C -l 30 -W", returnStdout: true)
            }
            return true
        } else {
            return false
        }
    }

    /**
     * Drop all users
     * @param databaseName          The name of the database
     * @param script                The global script parameter
     * @return                      True if users dropped, false if not
     */
    public static boolean dropAllUsers(String databaseName, Script script) {
        if (databaseExists(databaseName, false, script)) {
            def databaseServer = Config.getPropertyValue("azureDatabaseServer", script)
            def databaseAdminUsername = Config.getPropertyValue("serviceAdminDBUsername", script)
            def databaseAdminPassword = VaultKey.getSecuredValue("DatabaseDBPassword", script)

            def allUsersOutput = script.sh(script: "set +x;/opt/mssql-tools/bin/sqlcmd -d ${databaseName} -G -U ${databaseAdminUsername} -P ${databaseAdminPassword} -S 'tcp:${databaseServer}.database.windows.net,1433' -Q 'SET NOCOUNT ON; SELECT name as username FROM sysusers WHERE hasdbaccess = 1 AND islogin = 1 AND issqluser = 1' -C -l 30 -W -h-1", returnStdout: true)

            for (userName in allUsersOutput.split('\n')) {
                if (userName == "dbo" || userName == "") {
                    continue
                }

                script.echo("Dropping User: ${userName}")
                script.sh(script: "set +x;/opt/mssql-tools/bin/sqlcmd -d ${databaseName} -G -U ${databaseAdminUsername} -P ${databaseAdminPassword} -S 'tcp:${databaseServer}.database.windows.net,1433' -Q 'DROP USER ${userName}' -C -l 30 -W", returnStdout: true)
            }
            return true
        } else {
            return false
        }
    }

    /**
     * Drop all views
     * @param databaseName          The name of the database
     * @param script                The global script parameter
     * @return                      True if views dropped, false if not
     */
    public static boolean dropAllViews(String databaseName, Script script) {
        if (databaseExists(databaseName, false, script)) {
            def databaseServer = Config.getPropertyValue("azureDatabaseServer", script)
            def databaseAdminUsername = Config.getPropertyValue("serviceAdminDBUsername", script)
            def databaseAdminPassword = VaultKey.getSecuredValue("DatabaseDBPassword", script)

            def allViewsOutput = script.sh(script: "set +x;/opt/mssql-tools/bin/sqlcmd -d ${databaseName} -G -U ${databaseAdminUsername} -P ${databaseAdminPassword} -S 'tcp:${databaseServer}.database.windows.net,1433' -Q 'SET NOCOUNT ON; SELECT name FROM sys.views WHERE is_ms_shipped = 0' -C -l 30 -W -h-1", returnStdout: true)

            for (viewName in allViewsOutput.split('\n')) {
                if (viewName == "") {
                    continue
                }

                script.echo("Dropping View: ${viewName}")
                script.sh(script: "set +x;/opt/mssql-tools/bin/sqlcmd -d ${databaseName} -G -U ${databaseAdminUsername} -P ${databaseAdminPassword} -S 'tcp:${databaseServer}.database.windows.net,1433' -Q 'DROP VIEW ${viewName}' -C -l 30 -W", returnStdout: true)
            }
            return true
        } else {
            return false
        }
    }

    /**
     * Drop all sequences
     * @param databaseName          The name of the database
     * @param script                The global script parameter
     * @return                      True if sequences dropped, false if not
     */
    public static boolean dropAllSequences(String databaseName, Script script) {
        if (databaseExists(databaseName, false, script)) {
            def databaseServer = Config.getPropertyValue("azureDatabaseServer", script)
            def databaseAdminUsername = Config.getPropertyValue("serviceAdminDBUsername", script)
            def databaseAdminPassword = VaultKey.getSecuredValue("DatabaseDBPassword", script)

            def allSequencesOutput = script.sh(script: "set +x;/opt/mssql-tools/bin/sqlcmd -d ${databaseName} -G -U ${databaseAdminUsername} -P ${databaseAdminPassword} -S 'tcp:${databaseServer}.database.windows.net,1433' -Q 'SET NOCOUNT ON; SELECT name FROM sys.sequences WHERE is_ms_shipped = 0' -C -l 30 -W -h-1", returnStdout: true)

            for (sequenceName in allSequencesOutput.split('\n')) {
                if (sequenceName == "") {
                    continue
                }

                script.echo("Dropping Sequence: ${sequenceName}")
                script.sh(script: "set +x;/opt/mssql-tools/bin/sqlcmd -d ${databaseName} -G -U ${databaseAdminUsername} -P ${databaseAdminPassword} -S 'tcp:${databaseServer}.database.windows.net,1433' -Q 'DROP SEQUENCE ${sequenceName}' -C -l 30 -W", returnStdout: true)
            }
            return true
        } else {
            return false
        }
    }

    /**
     * Drop specific database
     * @param databaseName          The name of the database
     * @param script                The global script parameter
     * @return                      True if database dropped, else false
     */
    public static boolean dropDatabase(String databaseName, Script script) {
        if (databaseExists(databaseName, false, script)) {
            def databaseResourceGroup = Config.getPropertyValue("databaseResourceGroup", script)
            def databaseServer = Config.getPropertyValue("azureDatabaseServer", script)

            script.echo "Dropping database: " + databaseName
            def scriptToRun = "az sql db delete --name ${databaseName} --resource-group ${databaseResourceGroup} --server ${databaseServer} --yes --verbose"
            script.sh(script: scriptToRun)
            script.echo("Database dropped")
            return true
        } else {
            script.echo("No database found to drop")
            return false
        }
    }

    /**
     * Drop all FTS catalogs
     * @param databaseName          The name of the database
     * @param script                The global script parameter
     * @return                      True if catalogs dropped, false if not
     */
    public static boolean dropAllFTSCatalogs(String databaseName, Script script) {
        if (databaseExists(databaseName, false, script)) {
            def databaseServer = Config.getPropertyValue("azureDatabaseServer", script)
            def databaseAdminUsername = Config.getPropertyValue("serviceAdminDBUsername", script)
            def databaseAdminPassword = VaultKey.getSecuredValue("DatabaseDBPassword", script)

            def allFTSCatalogs = script.sh(script: "set +x;/opt/mssql-tools/bin/sqlcmd -d ${databaseName} -G -U ${databaseAdminUsername} -P ${databaseAdminPassword} -S 'tcp:${databaseServer}.database.windows.net,1433' -Q 'SET NOCOUNT ON; SELECT name FROM sys.fulltext_catalogs' -C -l 30 -W -h-1", returnStdout: true)

            for (catalogName in allFTSCatalogs.split('\n')) {
                if (catalogName == "") {
                    continue
                }

                script.echo("Dropping FTS Catalog: ${catalogName}")
                script.sh(script: "set +x;/opt/mssql-tools/bin/sqlcmd -d ${databaseName} -G -U ${databaseAdminUsername} -P ${databaseAdminPassword} -S 'tcp:${databaseServer}.database.windows.net,1433' -Q 'DROP FULLTEXT CATALOG ${catalogName}' -C -l 30 -W", returnStdout: true)
            }
            return true
        } else {
            return false
        }
    }
}

