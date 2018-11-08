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
SERVICE_PASSWORD=password123
DB_USER=ms_economicoperators
DB_PASSWORD=baseServicePassword1!
DB_HOST=localhost
DB_PORT=1401
DB_NAME=importnotification
```

## How to run

### Intellij setup

- Open the root of the repository
- import service as a maven project.  Open `Maven projects` from side tab, then click the `+` icon.  
Next select the `pom.xml` file from the service in the finder window.  Finally click the refresh icon
- Add the sdk to the project.  Click `File`>`Project Stucture`.  
Then select `1.8` in the dropdown of the project SDK section
- Select Edit Configurations and click the `+` icon.  Next select a new SpringBoot Application
- Give the configuration a name
- The main class should be: `uk.gov.defra.tracesx.economicoperators.EconomicOperatorsApplication`
- In the `use classpath of module` select the service from the dropdown 
- Add the environment variables as stated above
- Select `apply` and then `ok`
- Finally, click run

### Local setup

- From the service directory run `mvn clean install`
- Next expose the environment variables stated above
- Next run `java -jar target/TracesX_Certificate.jar` 
