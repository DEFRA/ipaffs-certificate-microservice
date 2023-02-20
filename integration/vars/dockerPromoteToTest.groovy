import defra.pipeline.config.Config
import defra.pipeline.octopus.OctoDocker

def call (String releaseVersion, String subscription) {
    OctoDocker.updateDockerToRelease(subscription, releaseVersion, this)
}
