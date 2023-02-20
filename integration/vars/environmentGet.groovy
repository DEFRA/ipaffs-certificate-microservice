#!/usr/bin/env groovy
import defra.pipeline.config.Config
import defra.pipeline.environments.EnvironmentActions

/**
 * Attempt to get a free pool by the following algorithm:
 * 1, Try and reserve a free pool, if found stop;
 * 2, Try and create a new pool and reserve it, if successful stop;
 * 3, Poll for up to 25 minutes for a free pool and reserve when found, if successful stop;
 * 4, Throw an error indicating no pools could be found.
 *
 * @param serviceName The name of the microservice being built
 * @param branchName  The branch name, used for creating the reservation tag
 */

import defra.pipeline.environments.EnvironmentMaxPoolsLimitException
import defra.pipeline.environments.EnvironmentNoPoolsFreeException
import hudson.AbortException

def call(String serviceName, String branchName, String freePoolTag = "freePoolTagValue") {

    String freePoolTagValue = Config.getPropertyValue(freePoolTag, this)

    def reservationTag = branchName
    if (reservationTag == "master") {
        reservationTag = "master-${BUILD_TAG}"
    }

    def reservedResourceGroupName = EnvironmentActions.reserveFreePool(serviceName, reservationTag, this)

    // If we can't reserve a pool try and create one and if that fails poll
    if (!reservedResourceGroupName) {
        def createdPoolResourceGroupName

        try {
            createdPoolResourceGroupName = EnvironmentActions.createPool(this)
            EnvironmentActions.tagResourceGroup(createdPoolResourceGroupName, serviceName, reservationTag, this)
            reservedResourceGroupName = createdPoolResourceGroupName
        } catch (EnvironmentMaxPoolsLimitException e) {
            // Poll for a group
            try {
                reservedResourceGroupName = EnvironmentActions.reserveFreePool(serviceName, reservationTag, 50, 30, this)
            } catch (EnvironmentNoPoolsFreeException e2) {
                throw new AbortException("Can't find a free pool, aborting")
            }
        }
    }

    return reservedResourceGroupName
}
