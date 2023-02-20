package defra.pipeline.azure.search

import groovy.json.JsonSlurper
import groovy.json.internal.LazyMap

import hudson.AbortException

import defra.pipeline.config.Config
import defra.pipeline.vault.VaultKey
import defra.pipeline.names.PoolTag

class AzureSearchActions {

    final String resourceGroupName
    final String serviceName
    final String subscription
    final Script script
    String azureSearchServiceName
    String azureSearchIndexName
    String azureSearchPublicIndexName
    String azureSearchServiceAdminAPIKey
    String azureSearchApiVersion

    AzureSearchActions(String resourceGroupName, String serviceName, String subscription, Script script) {
        this.resourceGroupName = resourceGroupName
        this.serviceName = serviceName
        this.subscription = subscription
        this.script = script
        script.echo "Setting subscription to ${subscription}"
    }

    void setup() {
        script.echo "Setting up AzureSearch"

        if (azureSearchServiceName)
            return

        def parametersMap = Config.getParameters(serviceName, subscription.toLowerCase(), script)

        if (!parametersMap.parameters.azureSearchServiceName)
            return

        azureSearchServiceName = parametersMap.parameters.azureSearchServiceName.value
        def poolNumber = resourceGroupName.tokenize('-')[-1]
        azureSearchIndexName = poolNumber + "-" + parametersMap.parameters.azureSearchIndexSuffix.value

        def publicIndexSuffix = parametersMap.parameters.azureSearchPublicIndexSuffix
        if (publicIndexSuffix) {
            azureSearchPublicIndexName = poolNumber + "-" + publicIndexSuffix.value
        }

        azureSearchApiVersion = parametersMap.parameters.azureSearchApiVersion.value
        def azureKeyForVault = Config.getPropertyValue("azureSearchAdminApiKey", script)
        azureSearchServiceAdminAPIKey = VaultKey.getSecuredValue(azureKeyForVault, script)
    }

    void truncate() {
        if (!hasAzureSearch())
            return

        script.echo "Truncating AzureSearch index"
        try {
            script.echo "Truncating index: ${azureSearchIndexName}"
            while (true) {
                String[] idsToRemove = getDocumentsForEnvironment(azureSearchIndexName)
                if (idsToRemove.size() == 0) {
                    break
                }
                deleteDocumentsById(idsToRemove, azureSearchIndexName)
            }
            script.echo "Finished truncating AzureSearch index: ${azureSearchIndexName}"
        } catch (Exception e) {
            script.echo "${e}"
            throw e
        }
    }

    void deleteSearchSchema() {
        if (!hasAzureSearch())
            return

        if (serviceName == "economicoperator-microservice") {
            deleteEconomicOperatorSearchSchema()
        } else {
            script.echo "Clearing AzureSearch Schema"
            def indexName = azureSearchIndexName
            def dataSourceName = replaceIndex(indexName, 'data-source')
            def indexerName = replaceIndex(indexName, 'indexer')

            deleteFromSearch("indexes", indexName)
            deleteFromSearch("datasources", dataSourceName)
            deleteFromSearch("indexers", indexerName)
        }
    }

    void deleteEconomicOperatorSearchSchema() {
        if (!hasAzureSearch())
            return

        script.echo "Clearing AzureSearch Schema"
        def privateDataSourceName = replaceIndex(azureSearchIndexName, 'data-source')
        def publicDataSourceName = replaceIndex(azureSearchPublicIndexName, 'data-source')
        def privateIndexerName = replaceIndex(azureSearchIndexName, 'indexer')
        def publicIndexerName = replaceIndex(azureSearchPublicIndexName, 'indexer')

        deleteFromSearch("indexes", azureSearchIndexName)
        deleteFromSearch("indexes", azureSearchPublicIndexName)
        deleteFromSearch("datasources", privateDataSourceName)
        deleteFromSearch("datasources", publicDataSourceName)
        deleteFromSearch("indexers", privateIndexerName)
        deleteFromSearch("indexers", publicIndexerName)
    }

    String[] getDocumentsForEnvironment(String indexName) {
        def url = "https://${azureSearchServiceName}.search.windows.net/indexes/${indexName}/docs/search?api-version=${azureSearchApiVersion}"
        def poolId = PoolTag.getId(resourceGroupName)
        def azureSearchQuery
        azureSearchQuery = """'{"queryType":"full","count":true,"search":"*","top":1000,"skip":0}'"""

        def object = postToSearch(url, azureSearchQuery)

        def ids = []
        object.value.each { obj ->
            ids << obj.id
        }

        return ids
    }

    private boolean hasAzureSearch() {
        setup()

        if (!azureSearchServiceName)
            return false

        return true
    }

    private void deleteDocumentsById(String[] documentIds, String indexName) {
        script.echo "Deleting ${documentIds.size()} documents from index " + indexName
        def url = "https://${azureSearchServiceName}.search.windows.net/indexes/" + indexName +
                        "/docs/index?api-version=${azureSearchApiVersion}"
        def azureSearchQuery = """'{"value":[""" + documentIds.collect { x ->
            """{"@search.action":"delete","id":"${x}"}"""
        }.join(",") + """]}'"""

        def object = postToSearch(url, azureSearchQuery)

        def ids = []
        object.value.each { obj ->
            ids << obj.key
        }

        script.echo "Deleted from Azure search: ${ids}"
    }

    private LazyMap postToSearch(url, query) {
        script.echo("Sending azure query: ${query}")
        script.echo("To URL: ${url}")

        def output = script.sh(script: """set +x; curl -X POST -H 'User-Agent: groovy-2.4.4' -H 'Content-Type: application/json' -H 'api-key: ${azureSearchServiceAdminAPIKey}' -d ${query} ${url}""", returnStdout: true)

        def jsonSlurper = new JsonSlurper()
        def object = null
        try {
            object = jsonSlurper.parseText(output)
        } catch (FileNotFoundException e) {
            throw new AbortException("Could not delete documents from index: ", e)
        }

        return object
    }

    private void deleteFromSearch(deleteType, name) {
        def statusCode = script.sh(script: """set +x; curl -s -S -o logs.out -w "%{http_code}" -X DELETE -H 'User-Agent: groovy-2.4.4' -H 'Content-Type: application/json' -H 'api-key: ${azureSearchServiceAdminAPIKey}' https://${azureSearchServiceName}.search.windows.net/${deleteType}/${name}?api-version=${azureSearchApiVersion}""", returnStdout: true)

        if (statusCode == "204") {
            script.echo("Deleted ${deleteType} entry ${name}")
        } else if (statusCode == "404") {
            script.echo("Couldn't find ${deleteType} entry ${name}, carrying on")
        } else {
            script.sh(script: "cat logs.out")
            script.sh(script: "rm -f logs.out")
            throw new AbortException("Could not delete ${deleteType}, got status code ${statusCode}")
        }
        script.sh(script: "rm -f logs.out")
    }

    private String replaceIndex(index, replacement) {
        int start = index.lastIndexOf("index");

        StringBuilder builder = new StringBuilder();
        builder.append(index.substring(0, start));
        builder.append(replacement);
        builder.append(index.substring(start + "index".length()));

        return builder.toString()
    }
}
