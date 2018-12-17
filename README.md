# Certificate Microservice

## Directory structure

- `database` - contains liquibase scripts with table structure
- `integration` - contains integration tests for certificate service
- `service` - contains Spring Boot project for certificate operators

### Environment Variables

export SERVICE_PORT=5060
export SERVICE_SCHEME=http  
export SERVICE_HOST=import-notification-commoditycategory-service  
export SERVICE_USER=importer  
export SERVICE_PASSWORD=password123  
export DATABASE_DB_USER=SA  
export DATABASE_DB_PASSWORD=dockerPassword1!  
export DATABASE_DB_PORT=1401  
export DATABASE_DB_NAME=importnotification  
export DATABASE_DB_HOST=import-notification-database  
export TRUST_SERVER_CERTIFICATE=true  
export DATABASE_DB_CONNECTION_STRING="jdbc:sqlserver://${DATABASE_DB_HOST}:${DATABASE_DB_PORT};database=${DATABASE_DB_NAME};encrypt=true;trustServerCertificate=${TRUST_SERVER_CERTIFICATE};hostNameInCertificate=*.database.windows.net;"  
export DB_USER=commodityCategoryServiceUser  
export DB_PASSWORD=Password1  
export DB_HOST=${IP_ADDRESS}  
export DB_PORT=1401  
export SERVICE_BASIC_AUTH_LOGIN=importer  
export SERVICE_BASIC_AUTH_PASSWORD=password123  
export DB_NAME=importnotification  
export BASE_SERVICE_DB_PASSWORD=Password1
export PERMISSIONS_SERVICE_SCHEME=http
export PERMISSIONS_SERVICE_HOST=localhost
export PERMISSIONS_SERVICE_PORT=5660
export PERMISSIONS_SERVICE_USERNAME=importer
export PERMISSIONS_SERVICE_PASSWORD=password123
export PERMISSIONS_SERVICE_CONNECTION_TIMEOUT=3000
export PERMISSIONS_SERVICE_READ_TIMEOUT=3000

## How to run

1. Start service by following `README.md` inside `service` directory
