package defra.pipeline.octopus

import defra.pipeline.BaseTest
import defra.pipeline.deploy.DeployQueries

import org.junit.Test

class OctoDockerTest extends BaseTest {

    @Test
    public void testOctoDockerReleaseComponentWithoutConfiguration() {
        writeFile("configuration/imports/deploylist/octopus/deployList.txt", """frontend-notification:no_database:no_configuration:1.0.400:release""")

        OctoDocker.updateDockerComponentToRelease("frontend-notification", "Octopus", "3.6.5", this)

        def dockerCommandsRan = testCommandRan "echo docker .*"
        assert dockerCommandsRan.size() == 3

        def snd_tag = "sndeuxfesacr001.azurecr.io/imports-release/frontend-notification:1.0.400"
        def octo_tag = "opsimpfesacr001.azurecr.io/imports-release/frontend-notification:3.6.5"

        assert dockerCommandsRan[0] == "echo docker pull '${snd_tag}'"
        assert dockerCommandsRan[1] == "echo docker tag '${snd_tag}' '${octo_tag}'"
        assert dockerCommandsRan[2] == "echo docker push '${octo_tag}'"
    }

    @Test
    public void testOctoDockerReleaseComponentWithConfiguration() {
        writeFile("configuration/imports/deploylist/octopus/deployList.txt", """notification-microservice:has_database:has_configuration:1.0.350:release""")

        OctoDocker.updateDockerComponentToRelease("notification-microservice", "Octopus", "3.6.5", this)

        def dockerCommandsRan = testCommandRan "echo docker .*"
        assert dockerCommandsRan.size() == 6

        def snd_tag = "sndeuxfesacr001.azurecr.io/imports-release/notification-microservice:1.0.350"
        def octo_tag = "opsimpfesacr001.azurecr.io/imports-release/notification-microservice:3.6.5"

        assert dockerCommandsRan[0] == "echo docker pull '${snd_tag}'"
        assert dockerCommandsRan[1] == "echo docker tag '${snd_tag}' '${octo_tag}'"
        assert dockerCommandsRan[2] == "echo docker push '${octo_tag}'"

        def snd_config_tag = "sndeuxfesacr001.azurecr.io/imports-release/notification-microservice-configuration:1.0.350"
        def octo_config_tag = "opsimpfesacr001.azurecr.io/imports-release/notification-microservice-configuration:3.6.5"

        assert dockerCommandsRan[3] == "echo docker pull '${snd_config_tag}'"
        assert dockerCommandsRan[4] == "echo docker tag '${snd_config_tag}' '${octo_config_tag}'"
        assert dockerCommandsRan[5] == "echo docker push '${octo_config_tag}'"
    }

    @Test
    public void testOctoDockerReleaseHotfixComponentWithConfiguration() {
        writeFile("configuration/imports/deploylist/octopus/deployList.txt", """notification-microservice:has_database:has_configuration:1.0.1H:hotfix""")

        OctoDocker.updateDockerComponentToRelease("notification-microservice", "Octopus", "3.6.6", this)

        def dockerCommandsRan = testCommandRan "echo docker .*"
        assert dockerCommandsRan.size() == 6

        def snd_tag = "sndeuxfesacr001.azurecr.io/imports-hotfix/notification-microservice:1.0.1H"
        def octo_tag = "opsimpfesacr001.azurecr.io/imports-release/notification-microservice:3.6.6"

        assert dockerCommandsRan[0] == "echo docker pull '${snd_tag}'"
        assert dockerCommandsRan[1] == "echo docker tag '${snd_tag}' '${octo_tag}'"
        assert dockerCommandsRan[2] == "echo docker push '${octo_tag}'"

        def snd_config_tag = "sndeuxfesacr001.azurecr.io/imports-hotfix/notification-microservice-configuration:1.0.1H"
        def octo_config_tag = "opsimpfesacr001.azurecr.io/imports-release/notification-microservice-configuration:3.6.6"

        assert dockerCommandsRan[3] == "echo docker pull '${snd_config_tag}'"
        assert dockerCommandsRan[4] == "echo docker tag '${snd_config_tag}' '${octo_config_tag}'"
        assert dockerCommandsRan[5] == "echo docker push '${octo_config_tag}'"
    }

    @Test
    public void testOctoDockerDeployAllComponents() {
        OctoDocker.updateDockerToRelease("Octopus", "3.6.5", this)

        def dockerCommandsRan = testCommandRan "echo docker .*"
        assert dockerCommandsRan.size() > 60 // Number of apps * 3, will be bigger as some have configuration containers

        def release_tag = DeployQueries.getReleaseNumber("Octopus", "frontend-notification", this)
        def snd_tag = "sndeuxfesacr001.azurecr.io/imports-release/frontend-notification:${release_tag}"
        assert "echo docker pull '${snd_tag}'" in dockerCommandsRan

        release_tag = DeployQueries.getReleaseNumber("Octopus", "notification-microservice", this)
        snd_tag = "sndeuxfesacr001.azurecr.io/imports-release/notification-microservice:${release_tag}"
        assert "echo docker pull '${snd_tag}'" in dockerCommandsRan

        def snd_config_tag = "sndeuxfesacr001.azurecr.io/imports-release/notification-microservice-configuration:${release_tag}"
        assert "echo docker pull '${snd_tag}'" in dockerCommandsRan

        def proxyRan = testCommandRan ".*imports-proxy.*"
        assert proxyRan.size() > 0
    }

}
