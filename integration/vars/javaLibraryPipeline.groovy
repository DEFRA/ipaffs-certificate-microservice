#!/usr/bin/env groovy

import defra.pipeline.config.Config;
import defra.pipeline.deploy.DeployQueries;

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def pipelineParams = [:]

    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()

    def deployables = DeployQueries.getListOfDeployableComponents(Config.getPropertyValue("secretscanningDeploymentList", this), this)

    pipeline {
        agent { label 'swarm' }
        environment {
            SERVICE_NAME = "$pipelineParams.SERVICE_NAME"
            SONARQUBE_PROJECT_NAME = "$pipelineParams.SONARQUBE_PROJECT_NAME"
            SERVICE_VERSION = "$pipelineParams.SERVICE_VERSION.${BUILD_NUMBER}"
        }
        options {
            ansiColor('xterm')
            timestamps()
            disableConcurrentBuilds()
        }
        parameters {
            booleanParam(name: 'PUSH_TO_ARTIFACTORY', defaultValue: false, description: 'Do you want to push hotfix to artifactory?')
            string(name: 'AFFECTED_VERSION', defaultValue: null, description: 'Enter affected version to be hotfixed e.g 2.0.103')
            string(name: 'HOTFIX_VERSION', defaultValue: '1', description: 'Enter hotfix version (1 if it’s the 1st time patching this release version)')
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
                    sh 'mvn -f pom.xml clean compile -DAPI_VERSION="${SERVICE_VERSION}" --settings ./settings/maven.xml'
                }
            }

            stage('Checkstyle') {
                steps {
                    getFile 'settings/maven.xml'
                    sh 'mvn -f pom.xml checkstyle:check --settings ./settings/maven.xml'
                }
            }

            stage('Unit Test') {
                steps {
                    sh script: 'mvn clean test jacoco:report -f pom.xml -DAPI_VERSION="' + "${SERVICE_VERSION}" + '" --settings ./settings/maven.xml'
                }
                post {
                    success {
                        junit '**/target/surefire-reports/*.xml'
                        jacoco(execPattern: '**/target/*.exec', exclusionPattern: '**/traceswsns/**/*,**/uk/gov/defra/tracesx/notification/api/*, **/uk/gov/defra/tracesx/selenium/**/*, **/uk/gov/defra/tracesx/integration/**/*',)
                    }
                }
            }

            stage('SonarQube Analysis') {
                steps {
                    runSonarQube("${BRANCH_NAME}")
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
        }

        post {
            always {
                step([$class: 'WsCleanup'])
            }
            failure {
                notifySlack("BUILD OF ${SERVICE_NAME} HAS FAILED: ${BUILD_URL}", "#FF9FA1")
            }
        }
    }
}
