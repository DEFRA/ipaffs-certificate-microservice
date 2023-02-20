#!/usr/bin/env groovy

/**
 * Tag the pool as free to use or indicate the pool has been reserved
 *
 * @param resourceGroupName   The pool we are freeing or specifying we've reserved
 * @param environmentReserve  Whether we're freeing or reserving
 * @param freePoolTag         Where to tag the env as regular or hotfix pool
 * @param notifyReturn        true if we should send a slack message when returning a pool
 */

import defra.pipeline.config.Config
import defra.pipeline.environments.EnvironmentQueries
import defra.pipeline.names.JenkinsName

def call(String resourceGroupName, Boolean environmentReserve = false, Boolean notifyReturn = false, String freePoolTag = "freePoolTagValue") {

    def buildPoolTag = Config.getPropertyValue(freePoolTag, this)

    if (resourceGroupName == null) {
        return
    }

    if (environmentReserve == false) {
        sleep(5)
        def allEnviroments = EnvironmentQueries.getAllResourceGroups(this)
        def enviromentExists = allEnviroments.find { it == resourceGroupName }
        if (enviromentExists) {
            def setTagsOnResourceGroup = "az group update --name ${resourceGroupName} --set tags.BuildPool=\"${buildPoolTag}\""
            sh(script: setTagsOnResourceGroup)
            if (notifyReturn) {
                notifySlack("Environment ${resourceGroupName} returned", "#b5cc1d")
            }
        }
    } else {
        echo "[INFO] ENVIRONMENT ${resourceGroupName} has been reserved"
        echo "[INFO] ONCE FINISHED PLEASE RETURN THE ENVIRONMENT POOL BY USING THE FOLLOWING LINK: https://jenkins-imports.azure.defra.cloud/job/Return_Azure_Environment/buildWithParameters?token=environmentReturn&RESOURCE_GROUP=${resourceGroupName}"

        JenkinsName jenkinsName = new JenkinsName()
        notifySlack("Environment ${resourceGroupName} reserved by ${jenkinsName.getBuildUser(currentBuild)} on ${BRANCH_NAME}", "#b5cc1d", "${BRANCH_NAME}")
    }
}
