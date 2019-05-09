## Introduction

Certificate service API used to store and retrieve certificate PDFs.  
The Certificate service API is written using the Spring Boot framework and has been created from the maven archetype

## Prerequisites 

In order to run the Certificte service API you must follow these prerequisites:

### Dependencies

In order to run the service you will need the following dependencies

- JDK v1.8
- Maven v3

**Important**: Before running the service, the database liquibase scripts must be run.  
Follow the instructions in the database `README.md` file

### Environment variables
The following environment variables must set before running the service 
(either in IDE or terminal window running the service from)

```
SERVICE_PORT=4862
SERVICE_USER=certificateServiceUser
SERVICE_PASSWORD=${SERVICE_PASSWORD}
DB_USER=SA
DB_PASSWORD=dockerPassword1!
DB_HOST=localhost
DB_PORT=1401
DB_NAME=importnotification
PERMISSIONS_SERVICE_SCHEME=http
PERMISSIONS_SERVICE_HOST=localhost
PERMISSIONS_SERVICE_PORT=5660
PERMISSIONS_SERVICE_USERNAME=importer
PERMISSIONS_SERVICE_PASSWORD=${SERVICE_PASSWORD}
PERMISSIONS_SERVICE_CONNECTION_TIMEOUT=3000
PERMISSIONS_SERVICE_READ_TIMEOUT=3000
```

### Id (JWT) token validation environment

* `SECURITY_JWT_JWKS`: A comma-separated list of jwks urls (the urls where the authentication providers publish their public signing keys)
* `SECURITY_JWT_ISS`: A comma-separated list of issuers. The token must contain an `iss` claim that matches one of these values.
* `SECURITY_JWT_AUD`: A comma-separated list of audiences. The token must contain an `aud` claim that matches one of these values.

Each of these comma-separated values must have the same number of elements. 

## How to run

### Intellij setup
``
- Open the root of the repository
- import service as a maven project.  Open `Maven projects` from side tab, then click the `+` icon.  
Next select the `pom.xml` file from the service in the finder window.  Finally click the refresh icon
- Add the sdk to the project.  Click `File`>`Project Stucture`.  
Then select `1.8` in the dropdown of the project SDK section
- Select Edit Configurations and click the `+` icon.  Next select a new SpringBoot Application
- Give the configuration a name
- The main class should be: `uk.gov.defra.tracesx.certificate.CertificateApplication`
- In the `use classpath of module` select the service from the dropdown 
- Add the environment variables as stated above
- Select `apply` and then `ok`
- Finally, click run

### Local setup

- From the service directory run `mvn clean install`
- Next expose the environment variables stated above
- Next run `java -jar target/TracesX_Certificate.jar` 

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
