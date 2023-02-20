#!/usr/bin/env groovy
/**
 * Check to see if a pool is already in use for the specified service and branch.
 * Throws an exception if an existing pool is found, failing the build.
 *
 * @param serviceName The name of the microservice being built
 * @param branchName The branch name, used for creating the reservation tag
 */

import defra.pipeline.environments.EnvironmentQueries
import hudson.AbortException

def call(String serviceName, String branchName) {

    def reservationTag = branchName
    if (reservationTag == "master") {
        reservationTag = "master-${BUILD_TAG}"
    }

    def existingPool = EnvironmentQueries.existingPoolWithTag(serviceName, reservationTag, this)
    if (existingPool != null) {
        throw new AbortException("${existingPool} is already in use for ${reservationTag} for ${serviceName}. Please try again after ${existingPool} has been released. You can release the pool now using this link: https://jenkins-imports.azure.defra.cloud/job/Return_Azure_Environment/buildWithParameters?token=environmentReturn&RESOURCE_GROUP=${existingPool}")
    }
}
