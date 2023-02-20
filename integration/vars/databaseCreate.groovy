#!/usr/bin/env groovy

import defra.pipeline.database.DatabaseActions

def call(String resourceGroupName, String serviceName, String subscription) {
    return DatabaseActions.createDatabase(serviceName, resourceGroupName, this)
}
