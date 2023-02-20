#!/usr/bin/env groovy

/**
 * The standard java pipeline
 */

import defra.pipeline.config.Config;
import defra.pipeline.deploy.DeployQueries

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def pipelineParams= [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()

    def resourceGroupName = null
    def jmeterBranch = params.JMETER_BRANCH ? "${params.JMETER_BRANCH}" : "master"
    def deployables = DeployQueries.getListOfDeployableComponents(Config.getPropertyValue("secretscanningDeploymentList", this), this)
    def dockerNetworkName = null;

    pipeline {
        agent {label 'swarm'}
        environment {
            SERVICE_NAME = "${env.BRANCH_NAME == "master" ? "$pipelineParams.SERVICE_NAME" : "$pipelineParams.SERVICE_NAME-feature"}"
            SONARQUBE_PROJECT_NAME = "$pipelineParams.SONARQUBE_PROJECT_NAME"
            SERVICE_VERSION = "${env.BRANCH_NAME == "master" ? "$pipelineParams.SERVICE_VERSION.${BUILD_NUMBER}" : "$pipelineParams.SERVICE_VERSION.${BUILD_NUMBER}F"}"
            ENVIRONMENT = "$pipelineParams.ENVIRONMENT"
            SELENIUM_BRANCH = "$pipelineParams.SELENIUM_BRANCH"
            RESOURCE_GROUP_NAME = "$pipelineParams.RESOURCE_GROUP_NAME"
            SERVICE_TYPE = "Java"
        }
        options {
            ansiColor('xterm')
            timestamps()
            disableConcurrentBuilds()
            gitLabConnection('Gitlab')
        }
        parameters {
            string(name: 'JMETER_BRANCH', defaultValue: 'master', description: 'Performance branch')
        }
        post {
            always {
                script {
                    if (env.BRANCH_NAME == 'master') {
                        //If pipeline has completed successfully will deploy the current promoted container to master
                        //If pipeline failed the staging container will not have been promoted and will resync master back to last good known configuration
                        echo "Pipeline has completed: Deploying last good known conatiner"
                        deployAppServiceVersion("${RESOURCE_GROUP_NAME}", "${SERVICE_NAME}", "${ENVIRONMENT}", true, this)
                    }
                }
                environmentReturn(resourceGroupName, false)
                step([$class: 'WsCleanup'])
            }
            failure {
                notifyTeams('red', "JOB: ${env.JOB_NAME} BUILD NUMBER: ${env.BUILD_NUMBER}", 'FAILED', 'mergeNotifications')
                updateGitlabCommitStatus name: 'build', state: 'failed'
            }
            success {
                notifyTeams('green', "JOB: ${env.JOB_NAME} BUILD NUMBER: ${env.BUILD_NUMBER}", 'SUCCESS', 'mergeNotifications')
                updateGitlabCommitStatus name: 'build', state: 'success'
            }
        }

        stages {

            stage('Code Secret Scanning'){
                when {
                    expression {deployables.contains(SERVICE_NAME)}
                }
                steps {
                    build job: 'Code-secret-scanning',
                        parameters: [
                            string(name: 'repository', value: "https://giteux.azure.defra.cloud/imports/$pipelineParams.SERVICE_NAME" +".git"),
                            string(name: 'branch', value: "master")
                        ]
                }
            }

            stage('Checkstyle') {
                steps {
                    getFile 'settings/maven.xml'
                    sh 'mvn -f service/pom.xml checkstyle:check --settings ./settings/maven.xml'
                }
            }

            stage('Compile') {
                steps {
                    getFile 'settings/maven.xml'
                    // remove bad dependencies fetch from artifactory
                    sh 'rm -Rf ~/.m2/repository/uk/gov/defra/tracesx'
                    script {
                        try {
                            sh script: 'mvn -f service/pom.xml clean compile -DAPI_VERSION="' + "${SERVICE_VERSION}" + '" --settings ./settings/maven.xml > logs.out 2>&1'
                        } catch (Exception e) {
                            sh script: 'cat logs.out && rm -f logs.out'
                            throw e
                        }
                    }
                    sh script: 'rm -f logs.out'
                }
                post {
                    success {
                        echo "Successfully compiled Java Proxy microservice"
                    }
                }
            }

            stage('Create OWASP report') {
                steps {
                    script {
                        try {
                            sh script: 'mvn -f service/pom.xml org.owasp:dependency-check-maven:check -Dformat=ALL --settings ./settings/maven.xml > logs.out 2>&1'
                            archiveArtifacts '**/service/target/dependency-check-report.html'
                            dependencyCheckPublisher pattern: '**/service/target/dependency-check-report.xml'
                            echo "Successfully generated OWASP report."
                        } catch (Exception e) {
                            sh script: 'cat logs.out && rm -f logs.out'
                            echo "Can't generate OWASP report for this microservice."
                        }
                    }
                    sh script: 'rm -f logs.out'
                }
            }

            stage('Unit Test') {
                steps {
                    script {
                        try {
                            sh script: 'mvn clean test jacoco:report -f service/pom.xml -DAPI_VERSION="' + "${SERVICE_VERSION}" + '" --settings ./settings/maven.xml > logs.out 2>&1'
                        } catch (Exception e) {
                            sh script: 'cat logs.out && rm -f logs.out'
                            throw e
                        }
                    }
                    sh script: 'rm -f logs.out'
                }
                post {
                    success {
                        junit '**/service/target/surefire-reports/*.xml'
                        jacoco(execPattern: '**/target/*.exec', exclusionPattern: '**/traceswsns/**/*,**/uk/gov/defra/tracesx/notification/api/*, **/uk/gov/defra/tracesx/selenium/**/*, **/uk/gov/defra/tracesx/integration/**/*', )
                        echo "Successfully ran unit tests for Java microservice"
                    }
                }
            }

            stage('SonarQube Analysis') {
                steps {
                    runSonarQube("${BRANCH_NAME}")
                }
            }

            stage('Azure Login') {
                steps {
                    azLoginClient()
                    dockerLogin("${ENVIRONMENT}")
                }
            }

            stage('Package') {
                steps {
                    script {
                        try {
                            sh script: 'mvn -f service/pom.xml resources:resources -DAPI_VERSION="' + "${SERVICE_VERSION}" + '" --settings ./settings/maven.xml > logs.out 2>&1'
                            sh script: 'mvn -f service/pom.xml package -DskipTests=true -Dowasp.skip=true -DAPI_VERSION="' + "${SERVICE_VERSION}" + '" --settings ./settings/maven.xml >> logs.out 2>&1'
                        } catch (Exception e) {
                            sh script: 'cat logs.out && rm -f logs.out'
                            throw e
                        }
                    }
                    sh script: 'rm -f logs.out'
                    downloadAppIns()
                }
                post {
                    success {
                        echo "Successfully packaged Java microservice"
                    }
                }
            }

            stage('Docker Build') {
                steps {
                    dockerBuild("${SERVICE_NAME}", "${ENVIRONMENT}", "${SERVICE_VERSION}", "${SERVICE_TYPE}")
                }
            }

            stage('Docker Push') {
                steps {
                    dockerPushToRegistry("${SERVICE_NAME}", "${ENVIRONMENT}", "${SERVICE_VERSION}")
                }
            }

            stage('Deploy Proxy') {
                steps {
                    environmentCreateIfNotExists("${RESOURCE_GROUP_NAME}")
                    deployAppInsights("${RESOURCE_GROUP_NAME}", "${ENVIRONMENT}")
                    deployAppServicePlan("${RESOURCE_GROUP_NAME}", "services-general-proxy", "${ENVIRONMENT}")
                    deployComponent("${RESOURCE_GROUP_NAME}",  "${SERVICE_NAME}", "${SERVICE_NAME}", "${ENVIRONMENT}", "${BRANCH_NAME}", "${SERVICE_VERSION}")
                }
            }

            stage('Integration Tests') {
                steps {
                    sh "mvn -f integration/pom.xml clean verify -Dskip.integration.tests=false -Dservice.base.url=https://${SERVICE_NAME}.azurewebsites.net --settings ./settings/maven.xml"
                }
                post {
                    always {
                        script {
                            try {
                                junit '**/integration/target/surefire-reports/*.xml'
                            } catch (Exception e) {
                                echo "Can't run junit reporter. Continuing assuming this isn't necessary for this microservice."
                            }
                        }
                        cucumber fileIncludePattern: '**/cucumber/**/*.json', reportTitle: 'Integration tests report'
                    }
                }
            }

            stage('Reserve environment for selenium tests') {
                steps {
                    lock ('environmentReservationsProxy') {
                        script {
                            resourceGroupName = environmentGet("${SERVICE_NAME}", "${BRANCH_NAME}")
                        }
                    }
                    environmentDeployParallel(resourceGroupName, "", "${ENVIRONMENT}", "", "${SERVICE_VERSION}")
                }
            }

            stage('Performance Tests') {
                steps {
                    echo "====== RUNNING PERFORMANCE TESTS ====="
                    runJmeterTests(resourceGroupName, "${BUILD_NUMBER}", jmeterBranch)
                }
            }

            stage('Selenium Tests') {
                steps {
                    script {
                        dockerNetworkName = createDockerNetwork(resourceGroupName, "${BUILD_NUMBER}")
                    }

                    startUpSeleniumGrid(resourceGroupName, "${BUILD_NUMBER}", dockerNetworkName)

                    withCredentials([string(credentialsId: 'JENKINS_GITLAB_TOKEN', variable: 'TOKEN')]) {
                        runSeleniumGridTests("frontend-decision", resourceGroupName, "${SELENIUM_BRANCH}", "sanity", "${env.BRANCH_NAME == "master" ? '' : '-feature'}", "${TOKEN}", "${BUILD_NUMBER}", true, "")
                        runSeleniumGridTests("frontend-notification", resourceGroupName, "${SELENIUM_BRANCH}", "sanity", "${env.BRANCH_NAME == "master" ? '' : '-feature'}", "${TOKEN}", "${BUILD_NUMBER}", true, "")
                        runSeleniumGridTests("frontend-control", resourceGroupName, "${SELENIUM_BRANCH}", "sanity", "${env.BRANCH_NAME == "master" ? '' : '-feature'}", "${TOKEN}", "${BUILD_NUMBER}", true, "")
                    }
                }
                post {
                    always {
                        cucumber fileIncludePattern: '**/cucumber/**/*.json', reportTitle: 'UI tests report'
                        tearDownSeleniumGrid(resourceGroupName, "${BUILD_NUMBER}")
                        sh "docker network rm ${dockerNetworkName}"
                    }
                    failure {
                        archiveCucumberReports()
                    }
                }
            }

            stage('Promote') {
                when {
                    environment name: 'BRANCH_NAME', value: 'master'
                }
                steps {
                    dockerPromote("${SERVICE_NAME}", "${ENVIRONMENT}", "${SERVICE_VERSION}", "${SERVICE_VERSION}")
                }
            }

            stage('Tag Master Branch on Successful Build') {
                when {
                    environment name: 'BRANCH_NAME', value: 'master'
                }
                steps {
                    withCredentials([string(credentialsId: 'JENKINS_GITLAB_TOKEN', variable: 'TOKEN')]) {
                        tagBranch("${BUILD_NUMBER}", "${TOKEN}", "${SERVICE_NAME}")
                    }
                }
            }
        }
    }
}
