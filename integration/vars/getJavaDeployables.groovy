import defra.pipeline.config.Config
import defra.pipeline.deploy.DeployQueries

def call() {
    List<String> deployables = DeployQueries.getListOfDeployableComponents(Config.getPropertyValue("sandpitDeploymentList", this), this)
    List<String> javaDeployables = deployables.findAll { !it.startsWith("frontend") }
    javaDeployables.add("imports-proxy")
    javaDeployables.remove("rds-wiremock-microservice")
    javaDeployables.remove("customer-wiremock-microservice")
    javaDeployables.sort(Comparator.naturalOrder())
    return javaDeployables
}
