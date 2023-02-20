import defra.pipeline.git.GitHelper

def call() {
    String dailyHotfixBranchName = "hotfix/IMTA-11039-daily-pipeline-check"
    GitHelper.setupGitConfig(this)

    withCredentials([string(credentialsId: 'JENKINS_GITLAB_TOKEN', variable: 'GITLAB_TOKEN')]) {
        sh(script: 'git remote set-url origin https://jenkins:$GITLAB_TOKEN@giteux.azure.defra.cloud/imports/pipeline-library.git')
        sh(script: 'git tag | xargs git tag -d')
        sh(script: 'git fetch origin \'refs/tags/*:refs/tags/*\'')
        latestReleaseTag = sh(script: 'git tag | sort -V | tail -n1', returnStdout: true)

        sh(script: "git checkout ${latestReleaseTag}")
        notificationMicroserviceConfiguration = sh(script: "grep ^notification-microservice resources/configuration/imports/deploylist/octopus/deployList.txt", returnStdout: true)
        notificationMicroserviceTag = notificationMicroserviceConfiguration.split(':')[3].split('\\.')[2]

        sh(script: 'git remote set-url origin https://jenkins:$GITLAB_TOKEN@giteux.azure.defra.cloud/imports/notification-microservice.git')
        sh(script: "git checkout master && git fetch --all && git reset --hard origin/master")
        sh(script: "git checkout -B ${dailyHotfixBranchName} ${notificationMicroserviceTag} && git push origin -f ${dailyHotfixBranchName}")
    }
}
