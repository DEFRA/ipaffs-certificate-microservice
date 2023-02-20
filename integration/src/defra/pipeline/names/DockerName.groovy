package defra.pipeline.names

import defra.pipeline.config.Config

class DockerName {

    /**
     * Get the name of a main docker container
     *
     * @param branchName       The name of the git branch
     * @param serviceName      The microservice name
     * @param subscription     The subscription, e.g. Sandpit
     * @param version          The version
     * @param isMasterRelease  True if this should be tagged with release
     * @param script           The global script parameter
     */
    public static String getNameAndTag(String branchName, String serviceName, String subscription, String version, boolean isMasterRelease, Script script) {

        def azureRegistry = ""

        if ( subscription == "Sandpit" || subscription == "SandpitASEv2" ) {
            azureRegistry = Config.getPropertyValue("azureSndContainerRegistry", script)
        } else {
            azureRegistry = Config.getPropertyValue("azureOpsContainerRegistry", script)
        }

        def projectName = Config.getPropertyValue("projectName", script)

        def dockerName = azureRegistry + "/${projectName.toLowerCase()}-"

        if (branchName == "master" && isMasterRelease) {
            dockerName = dockerName + "release/"
        } else if (branchName =~ 'hotfix*' && isMasterRelease) {
            dockerName = dockerName + "hotfix/"
        } else {
            dockerName = dockerName + Branches.getBranchPrefix(branchName) + "/"
        }

        dockerName = dockerName + serviceName.toLowerCase()

        if (version != "") {
          dockerName = dockerName + ":" + "${version}"
        }

        return dockerName
    }

    /**
     * Get the name of a configuration docker container
     *
     * @param branchName       The name of the git branch
     * @param serviceName      The microservice name
     * @param subscription     The subscription, e.g. Sandpit
     * @param version          The version
     * @param isMasterRelease  True if this should be tagged with release
     * @param script           The global script parameter
     */
    public static String getConfigurationNameAndTag(String branchName, String serviceName, String subscription, String version, boolean isMasterRelease, Script script) {
        return getNameAndTag(branchName, serviceName + '-configuration', subscription, version, isMasterRelease, script)
    }

}
