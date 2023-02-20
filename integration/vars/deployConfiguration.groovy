import defra.pipeline.names.DockerName
import defra.pipeline.names.Branches
import defra.pipeline.deploy.DeployActions


def call(String resourceGroupName, String serviceName, String subscription, String branchName, String version) {
    call(resourceGroupName, serviceName, subscription, branchName, version, false)
}

def call(String resourceGroupName, String serviceName, String subscription, String branchName, String version, boolean isMasterRelease) {
    String containerRepository = isMasterRelease == 'true' ? 'imports-release' : "imports-" + Branches.getBranchPrefix(branchName)
    String dockerNameTag = DockerName.getNameAndTag(branchName, serviceName, subscription, version, isMasterRelease, this)
    DeployActions.deployConfigurationOnly(resourceGroupName, serviceName, subscription, dockerNameTag, this)
}
