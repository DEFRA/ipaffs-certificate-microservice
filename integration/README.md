# Jenkins Shared Pipeline Library

This repository contains the shared pipeline library for the Jenkins CI server, developed for and used by the imports team. The pipeline was designed in order to facilitate development and release of a microservices architecture to the Azure cloud service. The design is such that any service can use this shared library in order to:
- Compile, unit test and package Java and NodeJS services;
- Build versioned docker containers for the main service;
- Build versioned docker containers that facilitate the setup of a component (e.g. setup DBs with liquibase, static data uploads, etc);
- Creating databases, running the docker setup containers and releasing the software using Azure Paas solutions;
- Running integration testing against a fully provisioned environment.

## Setup

This library has a variety of functions under vars that a microservice can consume once it has been setup. In order to setup a service to use this library one needs to add a line `@Library('pipeline-library') _` to the top of its Jenkinsfile. As an example, one could then call the `nodePipeline` global function that is kept under `vars` in this repository as follows:
```
@Library('pipeline-library@feature/refactor-with-unit-testing') _

javaPipeline {
    SERVICE_NAME = "notification-microservice"
    SONARQUBE_PROJECT_NAME = "Imports-notification-microservice"
    SERVICE_VERSION = "1.0"
    ENVIRONMENT = "Sandpit"
    SELENIUM_BRANCH = "master"
}
```

For further details on this setup see the following Jenkins URL: https://jenkins.io/doc/book/pipeline/shared-libraries/

### Gradle Tests 

The global functions kept under `vars` are intended to be fairly lightweight functions. In order to perform more complex tasks we also present lots of code under `src/` that is unit tested by the `test/` directory to provide faster feedback to developers than having to run under a pipeline. 

`Pipeline_Library` under the `Jenkins Pipelines` tab in the Jenkins UI is a multibranch pipeline for the pipeline-library repository. This pipeline is configured to run the unit tests located in the `test/` directory for feature branches and master. Once the pipeline has finished it will then pass feedback back into Gitlab, in order for a feature branch to be merged to master it must first pass unit tests.

In order to run the unit tests locally one can run:
```
./gradlew test --info
```

## Docker containers

There are two types of Docker containers built by this library, one for the service itself and one in order to run any pre-setup in order to facilitate running that service (e.g. DB scripts). The library will place both in docker ACR registries in Azure, labelled staging, and then once confirmed as acceptable these will be promoted to another Docker ACR registry, labelled release. This way we will have a registry from which we can reliably perform releases. In order for a container to be promoted to the release registry it will need a successful run of the pipeline for the master branch of the service.

### Service Docker Container

This is simply the application itself. The library function `dockerBuild` will build the docker image from the Dockerfile in the `services/` directory of the services repository.

### Configuration Docker Container

The pipeline library will build an additional docker container with the tag `-configuration` appended to the container from the `configuration` directory of the services repository. The library function `dockerBuildConfiguration` will build the docker image from the Dockerfile in the `configuration/` directory of the services repository.

## Config

The main configuration of the pipeline library is placed in `resources/settings/pipeline-properties.config`. This is currently imports specific but can be overridden with a file `settings/pipeline-properties.config` in the repository using the pipeline library.

## Pools

One concept that appears throughout the pipeline library is that of pools. A pool is a resource group is Azure that's treated in a slightly special way by the pipeline in places.

In order to faciliate developers we can run all feature branches of a microservice through a pipeline build in Jenkins to run unit and integration tests. To do this we use pooled environments, by this we mean that we can have a subscription, say `SUB001`, and the pipeline can automatically fully provision or upgrade a pool with name `SUB001-Pool-x` (where x is a number) to have the latest master from all microservices and then to release it's own container.

For a given subscription (say sandpit), we have a list of components given in `resources/configuration/${subscription}/deployList.txt`. The library function `environmentGet` will try and find the first free pool of the form `SUB001-Pool-x` and then `environmentImportsResync` will then release the full environment from `deployList.txt` and then we can run `deployComponent` to overwrite the component we are interested in. We will never build more pools than specified in the pipeline config item `maxNumPoolsLimit`.

Anything in `deployList.txt` that is listed as having a database will have a database provisioned for it and anything that is listed as having configuration will have it's configuration docker image ran against the pool and/or DB provisioned in order to setup the service.

Once a pool is reserved with `environmentGet` (using this should be locked as per javaPipeline so as to prevent race conditions) then the pool or resource group is tagged with BuildPool=<tag> where tag is some tag related to the branch, then anything else calling `environmentGet` will not receive the same pool. The pool can be returned with `environmentReturn` and should always be returned once finished with otherwise we can run out of resources. When we call `environmentReturn` we will retag with BuildPool as the tag set in the pipeline config item `freePoolTagValue`.

The first thing `environmentGet` will try and do is find a free pool, i.e. one without a tag of `freePoolTagValue`. If it finds one it will return that, if it doesn't it will try and create one and use that. If it has reached the limit of the number of pools it is allowed to create it will then retry every 30 seconds for 25 minutes to find another free pool. After this time has elapsed the job will fail.

## Outline of a Microservice

The pipeline will expect the following layout of a microservice in order to be built and released to an Azure environment:
`service/` - Should contain the service itself and a Dockerfile indicating how to build a docker image for releasing that service.
`configuration/` - Should contain setup scripts (such as liquibase) and should have a Dockerfile containing details of how to run these scripts against an environment. These will be ran for any pool that has `hasConfiguration` as the third column in `deployList.txt`.
`integration/` - Integration tests in the form of a maven project.
`settings/pipeline-properies.config` - Overrides pipeline properties as necessary.
