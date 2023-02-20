package defra.pipeline.names

class JenkinsName {

    public String getBuildUser(currentBuild) {
        return(currentBuild.rawBuild.getCause(Cause.UserIdCause).getUserId())
    }

}
