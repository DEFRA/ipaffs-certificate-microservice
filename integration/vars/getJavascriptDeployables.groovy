import defra.pipeline.config.Config
import defra.pipeline.deploy.DeployQueries

def call() {
    List<String> deployables = DeployQueries.getListOfDeployableComponents(Config.getPropertyValue("sandpitDeploymentList", this), this)
    return deployables.findAll { it.startsWith("frontend") }
}
