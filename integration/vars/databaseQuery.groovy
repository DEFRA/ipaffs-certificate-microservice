#!/usr/bin/env groovy

import defra.pipeline.database.DatabaseRunQuery

def call(String serviceName, String resourceGroup, String subscription, String dbName, String dbUser, String dbPassword, String query) {
    echo "Querying"

    def databaseQuery = new DatabaseRunQuery()

    if (dbUser != "" && dbPassword != "") {
        databaseQuery.queryDb(serviceName, subscription, dbName, dbUser, dbPassword, query, this)
    } else if (dbName != "") {
        databaseQuery.queryDb(serviceName, resourceGroup, subscription, query, dbName, this)
    } else {
        databaseQuery.queryDb(serviceName, resourceGroup, subscription, query, this)
    }

    echo "Database queried"
}
