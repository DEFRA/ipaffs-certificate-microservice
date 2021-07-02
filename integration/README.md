# Certificate Integration Tests

## Introduction

This project is a standalone maven project for running automated integration tests.

## How To Run

Before running the tests start up the service as per the root README
Also run `frontend-notification-microservice` - this is needed to serve stylesheets used to generate the certificate

## How To Test

You will find the appropriate value for _TEST_OPENID_TOKEN_SERVICE_AUTH_PASSWORD_ in /keybase/team/defra_devops/.env 
to use in the following command:

```
mvn clean verify \
  -Dtest.openid.service.url=https://openid-token-microservice.azurewebsites.net/ \
  -Dtest.openid.service.auth.username=poc \
  -Dtest.openid.service.auth.password=XXXX
  -Dservice.base.url=http://localhost:6060
  -Dfrontend.notification.base.url=http://localhost:8000
```

You can also run: ```cd ../ && ./runIntegration.sh```which is picking up needed values from .env

## Debugging 
Use IntelliJ to debug whichever tests you wish, with the following env variables (values found in the .env file):

```
TEST_OPENID_TOKEN_SERVICE_URL
TEST_OPENID_TOKEN_SERVICE_AUTH_USERNAME
TEST_OPENID_TOKEN_SERVICE_AUTH_PASSWORD
SERVICE_USERNAME=username
SERVICE_PASSWORD=password1
```

and these parameters:
`-Dservice.base.url=http://localhost:6060`
`-Dfrontend.notification.base.url=http://localhost:8000`