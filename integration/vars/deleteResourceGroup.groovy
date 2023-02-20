#!/usr/bin/env groovy

import defra.pipeline.azure.AzureActions
import defra.pipeline.names.JenkinsName

def call(String groupName) {
    echo "Deleting resource group ${groupName}"
    if (groupName != null && groupName.size() > 0) {
        JenkinsName jenkinsName = new JenkinsName()
        notifySlack("Environment being deleted by ${jenkinsName.getBuildUser(currentBuild)}", "#FF9FA1", "${BRANCH_NAME}")

        AzureActions.deleteResourceGroup(groupName, this)
        echo "Resource group ${groupName} deleted."
    } else {
        echo "NO RESOURCE GROUP SPECIFIED, NOTHING DELETED"
    }
}
