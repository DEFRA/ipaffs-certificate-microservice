#!/usr/bin/env groovy

/**
 * The standard node pipeline
 */

import defra.pipeline.config.Config;
import defra.pipeline.deploy.DeployQueries;

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
        options {
            ansiColor('xterm')
            timestamps()
            disableConcurrentBuilds()
            gitLabConnection('Gitlab')
        }
        environment {
            SERVICE_NAME = "$pipelineParams.SERVICE_NAME"
            SONARQUBE_PROJECT_NAME = "$pipelineParams.SONARQUBE_PROJECT_NAME"
            JENKINS_SERVICE_VERSION = "${env.BRANCH_NAME.startsWith("master") || env.BRANCH_NAME.startsWith("hotfix") ? "$pipelineParams.SERVICE_VERSION.${BUILD_NUMBER}" : "$pipelineParams.SERVICE_VERSION.${BUILD_NUMBER}999"}"
            ENVIRONMENT = "$pipelineParams.ENVIRONMENT"
            SERVICE_TYPE = "Node"
        }
        parameters {
            booleanParam(name: 'RESERVE_ENVIRONMENT', defaultValue: false, description: 'Do you want to reserve azure environment?')
            string(name: 'JMETER_BRANCH', defaultValue: 'master', description: 'Performance branch')
        }
        post {
            always {
                environmentReturn(resourceGroupName, params.RESERVE_ENVIRONMENT)
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

            stage('Check pool usage') {
                steps {
                    environmentCheck("${SERVICE_NAME}", "${BRANCH_NAME}")
                }
            }

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

            stage('Build Node') {
                steps {
                    echo ' Installing Packages and Running SASS script'
                    script {
                        try {
                            sh script: "npm --prefix ./service config set cache /home/jenkins_slave/.npm --global > logs.out 2>&1"
                            sh script: "npm --prefix ./service ci --prefer-offline >> logs.out 2>&1"
                            sh script: "cd service && ./sass.sh >> logs.out 2>&1"
                            sh script: "cd service && npm version ${JENKINS_SERVICE_VERSION} >> logs.out 2>&1"
                        } catch (Exception e) {
                            sh script: 'cat logs.out && rm -f logs.out'
                            throw e
                        }
                    }
                    sh script: 'rm -f logs.out'
                }
                post {
                    success {
                        echo "Successfully built node microservice"
                    }
                }
            }

            stage('Create npm audit report') {
                steps {
                    createNPMAuditReport()
                }
            }

            stage('Run Coverage Reports') {
                steps {
                    script {
                        try {
                            sh script: 'npm --prefix ./service run test-jenkins > logs.out 2>&1'
                        } catch (Exception e) {
                            sh script: 'cat logs.out && rm -f logs.out'
                            throw e
                        }
                    }
                    sh script: 'rm -f logs.out'
                    junit '**/service/report.xml'
                    cobertura(coberturaReportFile: "**/service/coverage/cobertura-coverage.xml")
                }
                post {
                    success {
                        echo "Successfully ran unit tests"
                    }
                }
            }

            stage('SonarQube Analysis') {
                steps {
                    runSonarQube("${BRANCH_NAME}", true)
                }
            }

            stage('Docker Build') {
                steps {
                    dockerBuild("${SERVICE_NAME}", "${ENVIRONMENT}", "${JENKINS_SERVICE_VERSION}", "${SERVICE_TYPE}")
                }
            }

            stage('Docker Push') {
                steps {
                    dockerPushToRegistry("${SERVICE_NAME}", "${ENVIRONMENT}", "${JENKINS_SERVICE_VERSION}")
                }
            }

            stage('===HOTFIX=== Please Select Release Candidate Version') {
                when {
                    branch 'hotfix/**'
                }
                steps {
                    withCredentials([string(credentialsId: 'JENKINS_GITLAB_TOKEN', variable: 'TOKEN')]) {
                        script {
                            resourceGroupName = setPipelineLibraryBranchAndResourceGroupName('pipeline-library', "${TOKEN}")
                        }
                    }
                    build job: 'Create_Hotfix_Pool', parameters: [string(name: 'VERSION', value: "${RELEASE_VERSION}"), string(name: 'RESOURCE_GROUP', value: "${resourceGroupName}"), string(name: 'SERVICE_NAME', value: "${SERVICE_NAME}"), string(name: 'ENVIRONMENT', value: "${ENVIRONMENT}"), string(name: 'BRANCH_NAME', value: "${BRANCH_NAME}"), string(name: 'SERVICE_VERSION', value: "${JENKINS_SERVICE_VERSION}")]
                }
            }

            stage('Parallel Deploy') {
                when {
                    not {
                        branch 'hotfix/**'
                    }
                }
                steps {
                    lock ('environmentReservations') {
                        script {
                            resourceGroupName = environmentGet("${SERVICE_NAME}", "${BRANCH_NAME}", 'freeHotfixPoolTagValue')
                        }
                    }
                    environmentDeployParallel(resourceGroupName, "${SERVICE_NAME}", "${ENVIRONMENT}", "${BRANCH_NAME}", "${JENKINS_SERVICE_VERSION}")
                }
            }

            stage('Performance Tests') {
                when {
                    not {
                        branch 'hotfix/**'
                    }
                }
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
                        runSeleniumGridTests("${SERVICE_NAME}", resourceGroupName, "${BRANCH_NAME}", "regression", "", "${TOKEN}", "${BUILD_NUMBER}", "")
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

            stage('===HOTFIX=== Promote Hotfix') {
                when {
                    branch 'hotfix/**'
                }
                steps {
                    input message: 'Do you want to promote the Hotfix Container?'
                    dockerPromote("${SERVICE_NAME}", "${ENVIRONMENT}", "${JENKINS_SERVICE_VERSION}", "${JENKINS_SERVICE_VERSION}")
                }
            }

            stage('Promote') {
                when {
                    environment name: 'BRANCH_NAME', value: 'master'
                }
                steps {
                    dockerPromote("${SERVICE_NAME}", "${ENVIRONMENT}", "${JENKINS_SERVICE_VERSION}", "${JENKINS_SERVICE_VERSION}")
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
