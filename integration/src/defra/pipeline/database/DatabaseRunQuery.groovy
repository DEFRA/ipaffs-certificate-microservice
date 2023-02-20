package defra.pipeline.database

import defra.pipeline.config.Config
import defra.pipeline.names.PoolTag
import defra.pipeline.resources.ResourceFiles
import defra.pipeline.vault.VaultKey

class DatabaseRunQuery {

    Script script;

    public void queryDb(String serviceName, String resourceGroupName, String subscription, String sqlQuery, Script script) {
        def parametersMap = Config.getParameters(serviceName, subscription.toLowerCase(), script)
        def readOnlyDatabase = parametersMap.parameters.readOnlyDatabase.value
        def serviceDBPasswordAD = parametersMap.parameters.serviceDBPasswordAD?.reference.secretName ?: "${serviceName}DatabasePasswordAD-snd"
        def dbPassword
        def serviceDBPasswordADSecretName = (readOnlyDatabase) ? "readOnlyDatabasePasswordAD-snd" : serviceDBPasswordAD
        dbPassword = VaultKey.getSecuredValue(serviceDBPasswordADSecretName, script)

        def dbUser = parametersMap.parameters.serviceDBUsernameAD.value
        def databaseName = PoolTag.getNameWithTag(serviceName, resourceGroupName)
        queryDb(serviceName, subscription, databaseName, dbUser, dbPassword, sqlQuery, script)
    }

    public void queryDb(String serviceName, String subscription, String databaseName, String dbUser, String dbPassword, String sqlQuery, Script script) {

        script.echo("Querying ${sqlQuery}")
        ResourceFiles.getBase64FileBinary("binaries/SQLRunner.jar-B64", "binaries/SQLRunner.jar", script)

        def parametersMap = Config.getParameters(serviceName, subscription.toLowerCase(), script)
        def sqlConnectionString = "jdbc:sqlserver://${parametersMap.parameters.serviceDBHost.value}:1433;user=${dbUser};password=${dbPassword};databaseName=${databaseName};encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;"
        if (dbUser.toLowerCase().matches(".*@defra.onmicrosoft.com")) {
            sqlConnectionString += "authentication=ActiveDirectoryPassword"
        }
        script.echo "${sqlConnectionString}"
        def command = "set +x && java -jar binaries/SQLRunner.jar '${sqlConnectionString}' '${sqlQuery.replaceAll(/'/, /'"'"'/)}'"
        def output = script.sh(returnStdout: true, script: command)
        script.echo("output=${output}")
    }

    public void queryDb(String serviceName, String resourceGroupName, String subscription, String sqlQuery, String databaseName, Script script) {
        def parametersMap = Config.getParameters(serviceName, subscription.toLowerCase(), script)
        def dbUser = parametersMap.parameters.serviceDBUsernameAD.value
        def dbPassword = VaultKey.getSecuredValue("${serviceName.split('-')[0]}-microserviceDatabasePasswordAD-snd", script)
        queryDb(serviceName, subscription, databaseName, dbUser, dbPassword, sqlQuery, script)
    }
}

