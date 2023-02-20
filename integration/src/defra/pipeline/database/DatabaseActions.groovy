package defra.pipeline.database

import defra.pipeline.names.PoolTag
import defra.pipeline.environments.EnvironmentQueries

class DatabaseActions {

    /**
     * Create a database for a service and resource group
     *
     * @param serviceName        The microservice name
     * @param resourceGroupName  The name of the resource group to create
     * @param script             The global script parameter
     * @return true if created else false
     */
    public static boolean createDatabase(String serviceName, String resourceGroupName, Script script) {

        if (serviceName == "economicoperator-microservice") {
            return createEconomicOperatorDatabases(resourceGroupName, script)
        }
        def dbName = PoolTag.getNameWithTag(serviceName, resourceGroupName)
        return DatabaseQueries.createDatabase(dbName, script)
    }

    /**
     * Create the economic operators databases
     *
     * @param resourceGroupName  The name of the resource group to create
     * @param script             The global script parameter
     * @return true if created else false
     */
    public static boolean createEconomicOperatorDatabases(String resourceGroupName, Script script) {

        def privateDbName = PoolTag.getNameWithTag("economicoperator-microservice", resourceGroupName)
        def publicDbName = PoolTag.getNameWithTag("economicoperator-microservice-public", resourceGroupName)

        def privateDbCreated = DatabaseQueries.createDatabase(privateDbName, script)
        def publicDbCreated = DatabaseQueries.createDatabase(publicDbName, script)

        return privateDbCreated && publicDbCreated
    }

    /**
     * Drop a database for a service and resource group
     *
     * @param serviceName        The microservice name
     * @param resourceGroupName  The name of the resource group to create
     * @param script             The global script parameter
     * @return true if the database exists and was dropped else false
     */
    public static boolean dropDatabase(String serviceName, String resourceGroupName, Script script) {

        if (serviceName == "economicoperator-microservice") {
            return dropDatabasesEconomicOperator(resourceGroupName, script)
        }

        checkPool(resourceGroupName, script)

        def dbName = PoolTag.getNameWithTag(serviceName, resourceGroupName)
        return DatabaseQueries.dropDatabase(dbName, script)
    }

    /**
     * Drop database for economic operator microservice
     *
     * @param serviceName        The microservice name
     * @param resourceGroupName  The name of the resource group to create
     * @param script             The global script parameter
     * @return true if the database exists and was dropped else false
     */
    public static boolean dropDatabasesEconomicOperator(String resourceGroupName, Script script) {

        checkPool(resourceGroupName, script)

        def privateDbName = PoolTag.getNameWithTag("economicoperator-microservice", resourceGroupName)
        def publicDbName = PoolTag.getNameWithTag("economicoperator-microservice-public", resourceGroupName)

        def privateDropped = DatabaseQueries.dropDatabase(privateDbName, script)
        def publicDropped = DatabaseQueries.dropDatabase(publicDbName, script)
        return privateDropped && publicDropped
    }

    /**
     * Drop all constraints in a database
     *
     * @param serviceName        The microservice name
     * @param resourceGroupName  The name of the resource group
     * @param script             The global script parameter
     * @return true if the foreign keys were dropped else false
     */
    public static boolean dropAllForeignKeys(String serviceName, String resourceGroupName, Script script) {

        checkPool(resourceGroupName, script)

        def dbName = PoolTag.getNameWithTag(serviceName, resourceGroupName)
        return DatabaseQueries.dropAllForeignKeys(dbName, script)
    }

    /**
     * Drop all constraints in the economic operator database
     *
     * @param resourceGroupName  The name of the resource group
     * @param script             The global script parameter
     * @return true if the foreign keys were dropped else false
     */
    public static boolean dropAllForeignKeysEconomicOperator(String resourceGroupName, Script script) {
        checkPool(resourceGroupName, script)

        def privateDbName = PoolTag.getNameWithTag("economicoperator-microservice", resourceGroupName)
        def publicDbName = PoolTag.getNameWithTag("economicoperator-microservice-public", resourceGroupName)

        def privateDropped = DatabaseQueries.dropAllForeignKeys(privateDbName, script)
        def publicDropped = DatabaseQueries.dropAllForeignKeys(publicDbName, script)

        return privateDropped && publicDropped
    }

    /**
     * Drop all tables in a database
     *
     * @param serviceName        The microservice name
     * @param resourceGroupName  The name of the resource group
     * @param script             The global script parameter
     * @return true if the tables were dropped else false
     */
    public static boolean dropAllTables(String serviceName, String resourceGroupName, Script script) {

        checkPool(resourceGroupName, script)

        def dbName = PoolTag.getNameWithTag(serviceName, resourceGroupName)
        return DatabaseQueries.dropAllTables(dbName, script)
    }

    /**
     * Drop all temporal tables
     *
     * @param serviceName        The microservice name
     * @param resourceGroupName  The name of the resource group
     * @param script             The global script parameter
     * @return true if the tables were dropped else false
     */
    public static boolean dropAllTemporalTables(String serviceName, String resourceGroupName, Script script) {

        checkPool(resourceGroupName, script)

        def dbName = PoolTag.getNameWithTag(serviceName, resourceGroupName)
        return DatabaseQueries.dropAllTemporalTables(dbName, script)
    }

    /**
     * Drop all tables in the economic operator database
     * @param resourceGroupName The name of the resource group
     * @param script            The global script parameter
     * @return true if the tables were dropped else false
     */
    public static boolean dropAllTablesEconomicOperator(String resourceGroupName, Script script) {

        checkPool(resourceGroupName, script)

        def privateDbName = PoolTag.getNameWithTag("economicoperator-microservice", resourceGroupName)
        def publicDbName = PoolTag.getNameWithTag("economicoperator-microservice-public", resourceGroupName)

        def privateDropped = DatabaseQueries.dropAllTables(privateDbName, script)
        def publicDropped = DatabaseQueries.dropAllTables(publicDbName, script)

        return privateDropped && publicDropped
    }

    /**
     * Drop all users in a database
     *
     * @param serviceName        The microservice name
     * @param resourceGroupName  The name of the resource group to create
     * @param script             The global script parameter
     * @return true if the users were dropped else false
     */
    public static boolean dropAllUsers(String serviceName, String resourceGroupName, Script script) {

        checkPool(resourceGroupName, script)

        def dbName = PoolTag.getNameWithTag(serviceName, resourceGroupName)
        return DatabaseQueries.dropAllUsers(dbName, script)
    }

    /**
     * Drop all users in the economic operator database
     *
     * @param resourceGroupName  The name of the resource group to create
     * @param script             The global script parameter
     * @return true if the users were dropped else false
     */
    public static boolean dropAllUsersEconomicOperator(String resourceGroupName, Script script) {

        checkPool(resourceGroupName, script)

        def privateDbName = PoolTag.getNameWithTag("economicoperator-microservice", resourceGroupName)
        def publicDbName = PoolTag.getNameWithTag("economicoperator-microservice-public", resourceGroupName)

        def privateDropped = DatabaseQueries.dropAllUsers(privateDbName, script)
        def publicDropped = DatabaseQueries.dropAllUsers(publicDbName, script)

        return privateDropped && publicDropped
    }

    /**
     * Drop all views in a database
     *
     * @param serviceName        The microservice name
     * @param resourceGroupName  The name of the resource group to create
     * @param script             The global script parameter
     * @return true if the views were dropped else false
     */
    public static boolean dropAllViews(String serviceName, String resourceGroupName, Script script) {

        checkPool(resourceGroupName, script)

        def dbName = PoolTag.getNameWithTag(serviceName, resourceGroupName)
        return DatabaseQueries.dropAllViews(dbName, script)
    }

    /**
     * Drop all views in the economic operator database
     *
     * @param resourceGroupName  The name of the resource group to create
     * @param script             The global script parameter
     * @return true if the views were dropped else false
     */
    public static boolean dropAllViewsEconomicOperator(String resourceGroupName, Script script) {

        checkPool(resourceGroupName, script)

        def privateDbName = PoolTag.getNameWithTag("economicoperator-microservice", resourceGroupName)
        def publicDbName = PoolTag.getNameWithTag("economicoperator-microservice-public", resourceGroupName)

        def privateDropped = DatabaseQueries.dropAllViews(privateDbName, script)
        def publicDropped = DatabaseQueries.dropAllViews(publicDbName, script)

        return privateDropped && publicDropped
    }

    /**
     * Drop all sequences in a database
     *
     * @param serviceName        The microservice name
     * @param resourceGroupName  The name of the resource group to create
     * @param script             The global script parameter
     * @return true if the sequences were dropped else false
     */
    public static boolean dropAllSequences(String serviceName, String resourceGroupName, Script script) {

        checkPool(resourceGroupName, script)

        def dbName = PoolTag.getNameWithTag(serviceName, resourceGroupName)
        return DatabaseQueries.dropAllSequences(dbName, script)
    }

    /**
     * Drop all sequences in the economic operator database
     *
     * @param resourceGroupName  The name of the resource group to create
     * @param script             The global script parameter
     * @return true if the sequences were dropped else false
     */
    public static boolean dropAllSequencesEconomicOperator(String resourceGroupName, Script script) {

        checkPool(resourceGroupName, script)

        def privateDbName = PoolTag.getNameWithTag("economicoperator-microservice", resourceGroupName)
        def publicDbName = PoolTag.getNameWithTag("economicoperator-microservice-public", resourceGroupName)

        def privateDropped = DatabaseQueries.dropAllSequences(privateDbName, script)
        def publicDropped = DatabaseQueries.dropAllSequences(publicDbName, script)

        return privateDropped && publicDropped
    }

    /**
     * Drop all fts catalogs in the economic operator database
     *
     * @param resourceGroupName  The name of the resource group to create
     * @param script             The global script parameter
     * @return true if the catalogs were dropped else false
     */
    public static boolean dropAllEconomicOperatorFTSCatalogs(String resourceGroupName, Script script) {
        checkPool(resourceGroupName, script)

        def privateDbName = PoolTag.getNameWithTag("economicoperator-microservice", resourceGroupName)
        def publicDbName = PoolTag.getNameWithTag("economicoperator-microservice-public", resourceGroupName)

        def privateDropped = DatabaseQueries.dropAllFTSCatalogs(privateDbName, script)
        def publicDropped = DatabaseQueries.dropAllFTSCatalogs(publicDbName, script)

        return privateDropped && publicDropped
    }

    private static boolean checkPool(String resourceGroupName, Script script) {
        if (!(resourceGroupName in EnvironmentQueries.getAllPools(script) || resourceGroupName in EnvironmentQueries.getAllHotfixPools(script))) {
            script.echo "Throwing DB Exception"
            throw new DatabaseAlteringNonPoolException();
        }
    }

    /**
     * Drop all fts catalogs in the service database
     *
     * @param resourceGroupName  The name of the resource group to create
     * @param script             The global script parameter
     * @return true if the catalogs were dropped else false
     */
    public static boolean dropAllFullTextCatalogs(String serviceName, String resourceGroupName, Script script) {
        checkPool(resourceGroupName, script)

        def dbName = PoolTag.getNameWithTag(serviceName, resourceGroupName)
        return DatabaseQueries.dropAllFTSCatalogs(dbName, script)
    }
}

