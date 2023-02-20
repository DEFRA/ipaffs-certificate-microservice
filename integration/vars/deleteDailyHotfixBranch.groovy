import defra.pipeline.git.GitHelper

def call() {
    String dailyHotfixBranchName = "hotfix/IMTA-11039-daily-pipeline-check"
    GitHelper.setupGitConfig(this)

    withCredentials([string(credentialsId: 'JENKINS_GITLAB_TOKEN', variable: 'GITLAB_TOKEN')]) {
        sh(script: 'git remote set-url origin https://jenkins:$GITLAB_TOKEN@giteux.azure.defra.cloud/imports/notification-microservice.git')
        sh(script: "git push origin --delete ${dailyHotfixBranchName}")
    }
}
