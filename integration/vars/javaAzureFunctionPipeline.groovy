#!/usr/bin/env groovy

/**
 * The standard java Azure Function pipeline
 */

import defra.pipeline.config.Config;
import defra.pipeline.deploy.DeployQueries;

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def pipelineParams= [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()

    def defaultSeleniumJobs = ['frontend-notification', 'frontend-decision', 'frontend-control', 'frontend-checks', 'frontend-bordernotification', 'frontend-upload']
    def resourceGroupName = null
    def seleniumBranchName = null
    def seleniumJobs = pipelineParams.SELENIUM_JOBS ? pipelineParams.SELENIUM_JOBS : defaultSeleniumJobs
    def jmeterBranch = params.JMETER_BRANCH ? "${params.JMETER_BRANCH}" : "master"
    def deployables = DeployQueries.getListOfDeployableComponents(Config.getPropertyValue("secretscanningDeploymentList", this), this)

    pipeline {
        agent {label 'swarm'}
        environment {
            SERVICE_NAME = "$pipelineParams.SERVICE_NAME"
            SONARQUBE_PROJECT_NAME = "$pipelineParams.SONARQUBE_PROJECT_NAME"
            JENKINS_SERVICE_VERSION = "${env.BRANCH_NAME.startsWith("master") || env.BRANCH_NAME.startsWith("hotfix") ? "$pipelineParams.SERVICE_VERSION.${BUILD_NUMBER}" : "$pipelineParams.SERVICE_VERSION.${BUILD_NUMBER}F"}"
            ENVIRONMENT = "$pipelineParams.ENVIRONMENT"
            SELENIUM_BRANCH = "$pipelineParams.SELENIUM_BRANCH"
            TEMPLATE_NAME = "${SERVICE_NAME}"
            SKIP_INTEGRATION = "$pipelineParams.SKIP_INTEGRATION"
            SKIP_SELENIUM = "$pipelineParams.SKIP_SELENIUM"
            SKIP_PERFORMANCE = "$pipelineParams.SKIP_PERFORMANCE"
            SERVICE_TYPE = "JavaAzureFunction"
        }
        options {
            ansiColor('xterm')
            timestamps()
            disableConcurrentBuilds()
            gitLabConnection('Gitlab')
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
                            sh script: 'mvn -f service/pom.xml clean compile -DAPI_VERSION="' + "${JENKINS_SERVICE_VERSION}" + '" --settings ./settings/maven.xml > logs.out 2>&1'
                        } catch (Exception e) {
                            sh script: 'cat logs.out && rm -f logs.out'
                            throw e
                        }
                    }
                    sh script: 'rm -f logs.out'
                }
                post {
                    success {
                        echo "Successfully compiled Java Azure Function"
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
                            sh script: 'mvn clean test jacoco:report -f service/pom.xml -DAPI_VERSION="' + "${JENKINS_SERVICE_VERSION}" + '" --settings ./settings/maven.xml > logs.out 2>&1'
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
                        echo "Successfully ran unit tests for Java Azure Function"
                    }
                }
            }

            stage('SonarQube Analysis') {
                steps {
                    runSonarQube("${BRANCH_NAME}")
                }
            }

            stage('Check pool usage') {
                steps {
                    environmentCheck("${SERVICE_NAME}", "${BRANCH_NAME}")
                }
            }

            stage('Package') {
                steps {
                    script {
                        try {
                            sh script: 'mvn -f service/pom.xml resources:resources -DAPI_VERSION="' + "${JENKINS_SERVICE_VERSION}" + '" --settings ./settings/maven.xml > logs.out 2>&1'
                            sh script: 'mvn -f service/pom.xml package -DskipTests=true -Dowasp.skip=true -DAPI_VERSION="' + "${JENKINS_SERVICE_VERSION}" + '" --settings ./settings/maven.xml >> logs.out 2>&1'
                        } catch (Exception e) {
                            sh script: 'cat logs.out && rm -f logs.out'
                            throw e
                        }
                    }
                    sh script: 'rm -f logs.out'
                }
                post {
                    success {
                        echo "Successfully packaged Java Azure Function"
                    }
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

            stage('===HOTFIX=== Please Select Release Candidate Version') {
                when {
                    branch 'hotfix/**'
                }
                steps {
                    withCredentials([string(credentialsId: 'JENKINS_GITLAB_TOKEN', variable: 'TOKEN')]) {
                        script {
                            resourceGroupName = setPipelineLibraryBranchAndResourceGroupName('pipeline-library', "${TOKEN}")
                            seleniumBranchName = env.RELEASE_VERSION
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
                            seleniumBranchName = "${SELENIUM_BRANCH}"
                        }
                    }
                    environmentDeployParallel(resourceGroupName, "${SERVICE_NAME}", "${ENVIRONMENT}", "${BRANCH_NAME}", "${JENKINS_SERVICE_VERSION}")
                }
            }

            stage('Integration Tests') {
                when {
                    expression {
                        return !shouldSkipStage("${SKIP_INTEGRATION}")
                    }
                }
                steps {
                    runServiceIntegrationTests("${SERVICE_NAME}", "${ENVIRONMENT}", "${BRANCH_NAME}", resourceGroupName, "regression")
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

            stage('Performance Tests') {
                when {
                    allOf {
                        expression {
                            return !shouldSkipStage("${SKIP_PERFORMANCE}")
                        }
                        not {
                            branch 'hotfix/**'
                        }
                    }
                }
                steps {
                    echo "====== RUNNING PERFORMANCE TESTS ====="
                    runJmeterTests(resourceGroupName, "${BUILD_NUMBER}", jmeterBranch)
                }
            }

            stage('Selenium Tests') {
                when {
                    expression {
                        return !shouldSkipStage("${SKIP_SELENIUM}")
                    }
                }
                steps {
                    startUpSeleniumGrid(resourceGroupName, "${BUILD_NUMBER}")

                    withCredentials([string(credentialsId: 'JENKINS_GITLAB_TOKEN', variable: 'TOKEN')]) {
                        script {
                            seleniumJobs.each {
                                runSeleniumGridTests("${it}", resourceGroupName, seleniumBranchName, "sanity", "", "${TOKEN}", "${BUILD_NUMBER}", true)
                                sh "mv integration/target/cucumber/default integration/target/cucumber/${it}"
                            }
                        }
                    }
                }
                post {
                    always {
                        cucumber fileIncludePattern: '**/cucumber/**/*.json', reportTitle: 'UI tests report'
                        tearDownSeleniumGrid(resourceGroupName, "${BUILD_NUMBER}")
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
                    dockerPromoteConfiguration("${SERVICE_NAME}", "${ENVIRONMENT}", "${JENKINS_SERVICE_VERSION}", "${JENKINS_SERVICE_VERSION}")
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
