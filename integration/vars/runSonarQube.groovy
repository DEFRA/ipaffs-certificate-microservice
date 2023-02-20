#!/usr/bin/env groovy
import hudson.AbortException

/**
 * Run runSonarQube
 *
 * @param branchName The name of the branch
 */

def call(String branchName, boolean isFrontend = false, boolean isSchema = false) {
    try {
        String path = sh(
                script: 'pwd',
                returnStdout: true
        ).trim()

        if (isFrontend) {
            String escapedPath = path.replaceAll("/", "\\\\/")

            sh(script: "mvn clean install -f service/pom.xml")
            sh(script: "sed -i.bak 's/path=\"test/path=\"service\\/test/g' service/coverage/unit.xml")
            sh(script: "sed -i.bak 's/${escapedPath}\\/service/service/g' service/coverage/lcov.info")
        } else if (isSchema) {
            sh(script: "./runJunit.sh")
        }

        sh(script: "echo \"\nsonar.branch.name=${branchName}\" >> sonar-project.properties")

        if (!isFrontend) {
            sh(script: 'echo "sonar.java.libraries=/usr/.m2" >> sonar-project.properties')
            sh(script: 'echo "sonar.java.test.libraries=/usr/.m2" >> sonar-project.properties')
        }

        echo('SonarQube Properties File:')
        sh(script: "cat sonar-project.properties")

        return sh(script: "docker run " +
                "--rm " +
                "--user=\"\$(id -u):\$(id -g)\" " +
                "-e SONAR_HOST_URL=\"https://vss-sonarqube.azure.defra.cloud/\" " +
                "-v \"${path}:/usr/src\" " +
                '-v $HOME/.m2:/usr/.m2 ' +
                "sonarsource/sonar-scanner-cli")
    } catch (AbortException e) {
        echo('SonarQube failed in lenient mode. Continuing as this is non-blocking.')
    }
}
