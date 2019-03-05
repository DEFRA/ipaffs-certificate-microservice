# Integration

## Introduction

This project is a standalone maven project for running automated integration tests.

## Pre-requisites

- JRE / JDK 8
- Maven v3
- [Lombok Plugin](https://plugins.jetbrains.com/plugin/6317-lombok-plugin) (only required for development)

## CI installation

- If there is no existing JDK, download and install 
  [Java 8 JDK](http://www.oracle.com/technetwork/java/javase/install-linux-64-rpm-138254.html)

## Running

### VM options

There are multiple VM options that need specifying to run the tests:

- `service.base.url` is the url (including scheme and port) for the service
- `auth.username` is basic auth username for the service
- `auth.password` is basic auth password for the service
- `skip.integration.tests` should be `false` to run the tests. See notes in the Maven section below.

The following properties / env variables are related to id token authentication.
 Property `test.openid.permissions.url` (or alternative env `TEST_OPENID_TOKEN_SERVICE_URL`)
- Property `test.openid.permissions.auth.username` (or alternative env `TEST_OPENID_TOKEN_SERVICE_AUTH_USERNAME`)
- Property `test.openid.permissions.auth.password` (or alternative env `TEST_OPENID_TOKEN_SERVICE_AUTH_PASSWORD`)

You can find these values in Keybase in the file `openid-token-microservice.txt`.

### Run the tests from Intellij

See VM options section (above) for more details on VM options

- The test classes are located at `src/test/java/uk/gov/defra/tracesx/integration`
- Run the test as a standard Java App (e.g. right click on the source file, Run TestAdminAuthentication)
- This should fail because the required runtime arguments have not been specified.
- Go to Run / Edit Configurations
- Paste VM Options available in `Running integration tests` directory on Keybase (you must be part of the `defra_devops` group)
to run tests against test environment
- Re-run the test and it should now pass
  - If tests are still not running, go to `integration` directory and run `mvn clean install`

### Maven

To run tests using maven, go to integration directory and run command available in
`Running integration tests` directory on Keybase (you must be part of the `defra_devops` group)
