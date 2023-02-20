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
            gitLabConnection('Gitlab')
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

            stage('Compile and Checkstyle') {
                steps {
                    getFile 'settings/maven.xml'
                    sh 'mvn -f pom.xml clean compile -DAPI_VERSION="${SERVICE_VERSION}" checkstyle:check --settings ./settings/maven.xml'
                }
            }

            stage('Create OWASP report') {
                steps {
                    script {
                        try {
                            sh script: 'mvn -f pom.xml package org.owasp:dependency-check-maven:aggregate -Dformat=ALL --settings ./settings/maven.xml -DskipTests=true > logs.out 2>&1'
                            archiveArtifacts '**/target/dependency-check-report.html'
                            dependencyCheckPublisher pattern: '**/target/dependency-check-report.xml'
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
                    sh 'mvn -f pom.xml package -DAPI_VERSION="${SERVICE_VERSION}" --settings ./settings/maven.xml -DskipTests=true -Dowasp.skip=true'
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
                notifyTeams('red', "JOB: ${env.JOB_NAME} BUILD NUMBER: ${env.BUILD_NUMBER}", 'FAILED', 'mergeNotifications')
                updateGitlabCommitStatus name: 'build', state: 'failed'
            }
            success {
                notifyTeams('green', "JOB: ${env.JOB_NAME} BUILD NUMBER: ${env.BUILD_NUMBER}", 'SUCCESS', 'mergeNotifications')
                updateGitlabCommitStatus name: 'build', state: 'success'
            }
        }
    }
}