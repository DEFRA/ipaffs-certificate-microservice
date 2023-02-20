#!/usr/bin/env groovy

import defra.pipeline.config.Config;
import defra.pipeline.deploy.DeployQueries;

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def pipelineParams= [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()

    def deployables = DeployQueries.getListOfDeployableComponents(Config.getPropertyValue("secretscanningDeploymentList", this), this)

    pipeline {
        agent {label 'swarm'}
        environment {
            SERVICE_NAME = "$pipelineParams.SERVICE_NAME"
            SONARQUBE_PROJECT_NAME = "$pipelineParams.SONARQUBE_PROJECT_NAME"
            SERVICE_VERSION = "$pipelineParams.SERVICE_VERSION.${BUILD_NUMBER}"
            BUILD_TAG = "${SERVICE_NAME}-SNAPSHOT-${BUILD_NUMBER}"
        }
        options {
            ansiColor('xterm')
            timestamps()
            disableConcurrentBuilds()
            gitLabConnection('Gitlab')
        }
        parameters {
            booleanParam(name: 'PUSH_TO_ARTIFACTORY', defaultValue: false, description: 'Do you want to push hotfix to artifactory?')
            string(name: 'AFFECTED_VERSION', defaultValue: null, description: 'Enter affected version to be hotfixed e.g 2.0.103')
            string(name: 'HOTFIX_VERSION', defaultValue: '1', description: 'Enter hotfix version (1 if it’s the 1st time patching this release version)')
        }
        post {
            always {
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

            stage('Compile') {
                steps {
                    getFile 'settings/maven.xml'
                    sh 'mvn -f pom.xml clean compile --settings ./settings/maven.xml'
                }
            }

            stage('Checkstyle') {
                steps {
                    getFile 'settings/maven.xml'
                    sh 'mvn -f pom.xml checkstyle:check --settings ./settings/maven.xml'
                }
            }

            stage('Unit Test Java') {
                steps {
                    sh script: 'mvn -f pom.xml test --settings ./settings/maven.xml'
                }
                post {
                    success {
                        junit '**/target/surefire-reports/*.xml'
                        jacoco(execPattern: '**/target/*.exec', exclusionPattern: '**/traceswsns/**/*,**/uk/gov/defra/tracesx/notification/api/*, **/uk/gov/defra/tracesx/selenium/**/*, **/uk/gov/defra/tracesx/integration/**/*',)
                    }
                }
            }

            stage('Unit Test Javascript') {
                steps {
                    script {
                        try {
                            sh script: 'npm --prefix ./imports-frontend-entities ci > logs.out 2>&1'
                            sh script: 'npm --prefix ./imports-frontend-entities run test-jenkins > logs.out 2>&1'
                        } catch (Exception e) {
                            sh script: 'cat logs.out && rm -f logs.out'
                            throw e
                        }
                    }
                    sh script: 'rm -f logs.out'
                    junit '**/imports-frontend-entities/report.xml'
                    cobertura(coberturaReportFile: "**/imports-frontend-entities/coverage/cobertura-coverage.xml")
                }
                post {
                    success {
                        echo "Successfully ran javascript unit tests"
                    }
                }
            }

            stage('Package') {
                steps {
                    sh 'mvn -f pom.xml resources:resources -DAPI_VERSION="${SERVICE_VERSION}" --settings ./settings/maven.xml -DskipTests=true'
                    sh 'mvn -f pom.xml package -DAPI_VERSION="${SERVICE_VERSION}" --settings ./settings/maven.xml -DskipTests=true'
                }
            }

            stage('Deploy Maven Artifact') {

                when {
                    environment name: 'BRANCH_NAME', value: 'master'
                }

                steps {
                    mvnDeploy("${BRANCH_NAME}", "pom.xml")
                }
            }

            stage('===HOTFIX=== Deploy Maven Artifact') {
                when {
                    allOf{
                        branch 'hotfix/**'
                        expression { params.PUSH_TO_ARTIFACTORY == true }
                        expression { params.AFFECTED_VERSION != null }
                    }
                }
                steps {
                    mvnDeploy("${BRANCH_NAME}", "pom.xml", true)
                }
            }

            stage('Build Node') {
                steps {
                    echo ' Installing Packages'
                    script {
                        try {
                            sh script: "npm --prefix ./imports-frontend-entities config set cache /home/jenkins_slave/.npm --global > logs.out 2>&1"
                            sh script: "npm --prefix ./imports-frontend-entities ci --prefer-offline >> logs.out 2>&1"
                        } catch (Exception e) {
                            sh script: 'cat logs.out && rm -f logs.out'
                            throw e
                        }
                    }
                    sh script: 'rm -f logs.out'
                }
                post {
                    success {
                        echo "Successfully built node elements for schema"
                    }
                }
            }

            stage('Publish Node'){

                steps{
                    echo 'Publishing node'
                    // TODO : Blocked until we have addressed artifactory issues reported by https://eaflood.atlassian.net/browse/IMTA-4608
                }

            }

            stage('SonarQube Analysis') {
                steps {
                    runSonarQube("${BRANCH_NAME}", false, true)
                }
            }
        }
    }
}
