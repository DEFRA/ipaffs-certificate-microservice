package defra.pipeline.git

import defra.pipeline.config.Config

class GitHelper {

    static void setupGitConfig(Script script) {
        String mvnReleaseGitUserEmail = Config.getPropertyValue('mvnReleaseGitUserEmail', script)
        String mvnReleaseGitUserName = Config.getPropertyValue('mvnReleaseGitUserName', script)

        script.sh(script: "git config user.email \"${mvnReleaseGitUserEmail}\"")
        script.sh(script: "git config user.name \"${mvnReleaseGitUserName}\"")
        script.sh(script: "git config --global push.default simple")
    }
}
