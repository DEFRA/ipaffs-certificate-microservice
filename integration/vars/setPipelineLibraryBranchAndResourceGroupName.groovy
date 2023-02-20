#!/usr/bin/env groovy

/**
 * Queries a specified repository for git tags, prompts user to select a release version
 * then returns a resource group name
 *
 * @param repoName  The name of the repository to query for tags
 * @param token     GitLab token Jenkins uses for permissions
 */
def call(String repoName, String token) {
    def releaseVersion
    if (params.IS_DAILY_PIPELINE) {
        releaseVersion = sh(script: "git ls-remote --tags https://jenkins:${token}@giteux.azure.defra.cloud/imports/${repoName}.git | cut -d/ -f3- | sort -V | tail -n1", returnStdout: true).trim()
    } else {
        def gitTags = sh(script: "git ls-remote --tags https://jenkins:${token}@giteux.azure.defra.cloud/imports/${repoName}.git | cut -d/ -f3-", returnStdout: true).trim()
        releaseVersion = input message: 'Select Release Version:', parameters: [choice(choices: "${gitTags}", description: 'Release Version for environment to create', name: '')]
    }
    echo "Selected release version: $releaseVersion"
    env.RELEASE_VERSION = releaseVersion

    def poolSuffix = releaseVersion.replaceAll("\\.", "")

    return "SNDIMPINFRGP001-Hotfix-Pool-${poolSuffix}"
}
