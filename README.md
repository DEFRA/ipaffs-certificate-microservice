# Certificate Microservice

## Introduction

Certificate service API used to store and retrieve certificate PDFs.  
The Certificate service API is written using the Spring Boot framework and has been created from the maven archetype

### Dependencies

In order to run the service you will need the following dependencies

- JDK v11
- Maven v3

## Set up
Ensure that you have the necessary configuration to resolve dependencies from Artifactory: https://eaflood.atlassian.net/wiki/spaces/IT/pages/1047823027/Artifactory

Copy `.env` file from `/keybase/team/defra_devops/.env` into the root of this project and run the service : 

From the service directory, run with:

```mvn spring-boot:run```

***For this service to work you need running frontend-notification-microservice.***

### Manual testing
Manually test endpoint

    http://localhost:6060/admin/health-check
    
And test endpoints with [appropriate security headers](https://eaflood.atlassian.net/wiki/spaces/IM/pages/1171489028/Generating+Security+Headers+for+Backend+Calls+in+Development)
    
    POST http://localhost:6060/certificate/{REFERENCE NUMBER}?url={URL_TO_FRONTEND}
    with body containing:
    certificateHtmlPayload

### Unit tests

From the service directory, run with:

    mvn test
    
The coverage report can be created with:

    mvn clean test jacoco:report
    
The coverage report can then be viewed by opening the `target/site/jacoco/index.html` file in your browser.

### Integration Tests

See Readme in integration directory

## How To Debug

Debug CertificateApplication with active profile set to `local`
  
### Properties
The following properties are used by the application (see required values in the .env file)

```
SERVICE_PORT
SERVICE_USER
SERVICE_PASSWORD
PERMISSIONS_SERVICE_SCHEME
PERMISSIONS_SERVICE_HOST
PERMISSIONS_SERVICE_PORT
PERMISSIONS_SERVICE_USERNAME
PERMISSIONS_SERVICE_PASSWORD
PERMISSIONS_SERVICE_CONNECTION_TIMEOUT
PERMISSIONS_SERVICE_READ_TIMEOUT
SECURITY_JWT_JWKS
SECURITY_JWT_ISS
SECURITY_JWT_AUD
FRONTEND_NOTIFICATION_SCHEMA
FRONTEND_NOTIFICATION_HOST
FRONTEND_NOTIFICATION_PORT
FRONTEND_NOTIFICATION_SERVICE_USERNAME
FRONTEND_NOTIFICATION_SERVICE_PASSWORD
```

### Id (JWT) token validation environment

* `SECURITY_JWT_JWKS`: A comma-separated list of jwks urls (the urls where the authentication providers publish their public signing keys)
* `SECURITY_JWT_ISS`: A comma-separated list of issuers. The token must contain an `iss` claim that matches one of these values.
* `SECURITY_JWT_AUD`: A comma-separated list of audiences. The token must contain an `aud` claim that matches one of these values.

Each of these comma-separated values must have the same number of elements. 

### Logging into Azure for building Docker images

We've now moved to using the sandpit Docker ACR in Azure for our Docker base images. In order to build Docker images you'll now need to login to Azure and the ACR as follows.

You need to install the `az` cli tools. This can be done following the simple instructions here: https://docs.microsoft.com/en-us/cli/azure/install-azure-cli-macos?view=azure-cli-latest or indeed by just running
```
brew install azure-cli
```
Now login to Azure with the following command:
```
az login
```
This will prompt open a new tab in your browser and ask you to login. Login with your usual username and password for the VPN, the one ending in `@Defra.onmicrosoft.com`. After successfully logging in run the following command:
```
az acr login --name sndeuxfesacr001 --subscription AZR-SND
```
You should receive a message saying `Login Succeeded`. You should be able to build the Docker images as normal now.

You should only need to do this for the first time you pull the base image. In case of problems try removing `~/.azure` in your home directory and retry.

### Environment variables 
```
export SERVICE_PORT=4862
export SERVICE_USER=certificateServiceUser
export SERVICE_PASSWORD=${SERVICE_PASSWORD}
export DB_USER=SA
export DB_PASSWORD=dockerPassword1!
export DB_HOST=localhost
export DB_PORT=1401
export DB_NAME=importnotification
export PERMISSIONS_SERVICE_SCHEME=http
export PERMISSIONS_SERVICE_HOST=localhost
export PERMISSIONS_SERVICE_PORT=5660
export PERMISSIONS_SERVICE_USERNAME=importer
export PERMISSIONS_SERVICE_PASSWORD=${SERVICE_PASSWORD}
export PERMISSIONS_SERVICE_CONNECTION_TIMEOUT=3000
export PERMISSIONS_SERVICE_READ_TIMEOUT=3000
export SECURITY_JWT_JWKS
export SECURITY_JWT_ISS
export SECURITY_JWT_AUD
export FRONTEND_NOTIFICATION_SCHEMA
export FRONTEND_NOTIFICATION_HOST
export FRONTEND_NOTIFICATION_PORT
export FRONTEND_NOTIFICATION_SERVICE_USERNAME
export FRONTEND_NOTIFICATION_SERVICE_PASSWORD
   ```