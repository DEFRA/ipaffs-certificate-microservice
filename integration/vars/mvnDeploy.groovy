import defra.pipeline.git.GitHelper

def call(String branchName, String pomFile, boolean isHotfix = false) {

    // TODO: would be nice if this was a conditional step instead
    result = sh(script: "git log -1 | grep '\\[maven-release-plugin\\]'", returnStatus: true)
    if(result != 0) {
        echo "Running mvn release..."
    } else {
        echo "SKIPPING mvn release: the previous commit was a release commit"
        return
    }

    GitHelper.setupGitConfig(this)

    withCredentials([
            usernamePassword(credentialsId: 'artifactoryImportsCreds', passwordVariable: 'RELEASE_PASSWORD', usernameVariable: 'USERNAME'),
            string(credentialsId: 'JENKINS_GITLAB_TOKEN', variable: 'GITLAB_TOKEN')]) {
        sh(script: 'git remote set-url origin https://jenkins:$GITLAB_TOKEN@giteux.azure.defra.cloud/imports/$SERVICE_NAME.git')
        sh(script: "git checkout -f ${branchName} && git pull origin ${branchName}")

        if (isHotfix) {
            hotfixReleaseVersion = "${params.AFFECTED_VERSION}-HOTFIX-${params.HOTFIX_VERSION}"
            hotfixReleaseSnapshotVersion = "${hotfixReleaseVersion}-SNAPSHOT"
            sh(script: "mvn -e -f ${pomFile} -B release:update-versions -DdevelopmentVersion=${hotfixReleaseSnapshotVersion}")
            sh(script: "git add . && git commit -m '[maven-release-plugin] set snapshot version for hotfix release ${hotfixReleaseSnapshotVersion}' && git push origin ${branchName}")
            sh(script: "mvn -e -f ${pomFile} -B -Dtag=${hotfixReleaseVersion} release:prepare release:perform --settings ./settings/maven.xml -DskipTests=true")
        } else {
            sh(script: "mvn -e -f ${pomFile} -B release:prepare release:perform --settings ./settings/maven.xml -DskipTests=true")
        }
    }
}
