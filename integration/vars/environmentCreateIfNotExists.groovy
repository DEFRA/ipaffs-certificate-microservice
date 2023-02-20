#!/usr/bin/env groovy

import defra.pipeline.environments.EnvironmentActions
import defra.pipeline.environments.EnvironmentQueries
import hudson.AbortException
import defra.pipeline.config.Config
import defra.pipeline.azure.AzureQueries
import defra.pipeline.azure.AzureActions

def call(String environmentName) {

    def allEnviroments = EnvironmentQueries.getAllResourceGroups(this)
    echo "Looking for: ${environmentName}"
    envExists = allEnviroments.find { it == environmentName }
    if (!envExists) {
        EnvironmentActions.createResourceGroup(environmentName, this)
    }
}

