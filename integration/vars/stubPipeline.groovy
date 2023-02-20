#!/usr/bin/env groovy

/**
 * Stub pipeline, to just deploy a container stub
 */

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def pipelineParams= [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()

    def resourceGroupName = null

    pipeline {
        agent {label 'swarm'}
        environment {
            SERVICE_NAME = "$pipelineParams.SERVICE_NAME"
            JENKINS_SERVICE_VERSION = "1.0.0"
            ENVIRONMENT = "$pipelineParams.ENVIRONMENT"
            SERVICE_TYPE = "Stub"
        }
        options {
            ansiColor('xterm')
            timestamps()
            disableConcurrentBuilds()
            gitLabConnection('Gitlab')
        }
        parameters {
            booleanParam(name: 'RESERVE_ENVIRONMENT', defaultValue: false, description: 'Do you want to reserve azure environment?')
        }
        post {
            always {
                environmentReturn(resourceGroupName, params.RESERVE_ENVIRONMENT)
                step([$class: 'WsCleanup'])
            }
            failure {
                notifySlack("BUILD OF ${SERVICE_NAME} HAS FAILED: ${BUILD_URL}", "#FF9FA1")
                updateGitlabCommitStatus name: 'build', state: 'failed'
            }
            success {
                updateGitlabCommitStatus name: 'build', state: 'success'
            }
        }

        stages {

            stage('Azure Login') {
                steps {
                    azLoginClient()
                    dockerLogin("${ENVIRONMENT}")
                }
            }

            stage('Check pool usage') {
                steps {
                    environmentCheck("${SERVICE_NAME}", "${BRANCH_NAME}")
                }
            }

            stage('Docker Build') {
                steps {
                    dockerBuild("${SERVICE_NAME}", "${ENVIRONMENT}", "${JENKINS_SERVICE_VERSION}", "${SERVICE_TYPE}")
                    dockerBuildConfigurationSetup("${SERVICE_NAME}", "${ENVIRONMENT}", "${JENKINS_SERVICE_VERSION}")
                }
            }

            stage('Docker Push') {
                steps {
                    dockerPushToRegistry("${SERVICE_NAME}", "${ENVIRONMENT}", "${JENKINS_SERVICE_VERSION}")
                    dockerPushConfigurationToRegistry("${SERVICE_NAME}", "${ENVIRONMENT}", "${JENKINS_SERVICE_VERSION}")
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

            stage('Promote') {
                when {
                    environment name: 'BRANCH_NAME', value: 'master'
                }
                steps {
                    dockerPromote("${SERVICE_NAME}", "${ENVIRONMENT}", "${JENKINS_SERVICE_VERSION}", "${JENKINS_SERVICE_VERSION}")
                    dockerPromoteConfiguration("${SERVICE_NAME}", "${ENVIRONMENT}", "${JENKINS_SERVICE_VERSION}", "${JENKINS_SERVICE_VERSION}")
                }
            }
        }
    }
}
