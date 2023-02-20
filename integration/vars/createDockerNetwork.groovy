#!/usr/bin/env groovy
import defra.pipeline.names.PoolTag

/**
 * Create Docker network

 * @param resourceGroupName The resource group to run against
 * @param buildNumber is the current build
 * */

def call(String resourceGroupName, String buildNumber) {
    def poolTag = "${PoolTag.getId(resourceGroupName)}"
    def buildName = "${poolTag}_${buildNumber}"
    def dockerNetworkName = "${buildName}_network"
    sh "docker network create ${dockerNetworkName}"
    return dockerNetworkName
}

