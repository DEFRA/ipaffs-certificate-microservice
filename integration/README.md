# Integration

## Introduction

This project is a standalone maven project for running automated integration tests.

## Pre-requisites

- JRE / JDK 8
- Maven v3
- [Lombok Plugin](https://plugins.jetbrains.com/plugin/6317-lombok-plugin) (only required for development)

## CI installation

(Cent OS)

- If there is no existing JDK, download and install 
  [Java 8 JDK](http://www.oracle.com/technetwork/java/javase/install-linux-64-rpm-138254.html)

## Running

### VM options

There are multiple VM options that need specifying to run the tests:

- `service.base.url` - base url for the service
- `auth.username` - auth user name
- `auth.password` - password for the user
- `environment.name` - name of environment the service is deployed in
- `branch.prefix` - name (prefix) of current branch

Passwords can be set from environment variables. For example, `source database/local_vars.sh`

The following properties / env variables are related to id token authentication.
 Property `test.openid.service.url` (or alternative env `TEST_OPENID_TOKEN_SERVICE_URL`)
- Property `test.openid.service.auth.username` (or alternative env `TEST_OPENID_TOKEN_SERVICE_AUTH_USERNAME`)
- Property `test.openid.service.auth.password` (or alternative env `TEST_OPENID_TOKEN_SERVICE_AUTH_PASSWORD`)

You can find these values in Keybase in the file `openid-token-microservice.txt`.

### Run the tests from Intellij

See VM options section (above) for more details on VM options

- Right click on @Test or on test class, click run test.
- This should fail because of missing VM options
- Select the following:
  - Run / Edit Configurations
  - Under 'Name' - enter 'Integration Tests'
  - Add the 'VM Options'. See the sample below or the definitions above.
  - Click OK
- Re-run the test and it should now pass

Example VM Options for IntelliJ

    -Dskip.integration.tests=false
    -Dit.test=WIPTestRunner
    -Dservice.base.url=http://localhost:<SERVICE_PORT>
    -Dauth.username=<SERVICE_USER> 
    -Dauth.password=<SERVICE_PASSWORD>
    -Denvironment.name=local
    -Dbranch.prefix=imta-3494

Fill in the passwords from `docker-local/local_vars.sh` or your own configuration for these services.

### Running individual tests

Right click on @Test or on test class, click run test.

### Maven

VM options can be passed into maven using `-D<PARAM>=<VALUE>`

#### Maven Example - run for entire project

> **Tip**: run `$ source docker-local/local_vars.sh` to export environment variables that work with the
Docker images. You will need to run this for every terminal in which you want to use these variables.

    mvn test -f integration/pom.xml 
        -Dservice.base.url=<CERTIFICATE_OPERATOR_URL>
        -Dauth.username=<CERTIFICATE_USER_NAME>
        -Dauth.password=<CERTIFICATE_PASSWORD>
        -Denvironment.name=local
        -Dbranch.prefix=imta-3494
