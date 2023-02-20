************* ARM TEMPLATES *****************
Folder Structure:

configuration/imports/${AZURE_RESOURCE}

Within each folder of an azure resource that will be deploying using arm there will be:
1. templates - Folder which contains the azure template (azuredeploy.json)
2. parameters - within the parameters folder there will be folders that will supply the specific parameter files per environment (azuredeploy.parameters.json)
   for example: 'sandpit/' 'sandpitasev2/' 

Azure Resources:

application_insights:
  - Deploys application insights instance on azure 
  - https://docs.microsoft.com/en-us/azure/azure-monitor/app/app-insights-overview

appserviceplans:
  - Deploys the app service plans on which the Web App Services will use the compute resources
  - https://docs.microsoft.com/en-us/azure/app-service/overview-hosting-plans

deploylist:
  - Determines the list of microservices which will be deployed into each environment

web_app_services:
  - Hosting web applications; Node or Java eg notification-microservice
  - https://docs.microsoft.com/en-us/azure/app-service/overview
  - environment variables for the application to use will be configured here when deployed to azure in 'appSettings' section 

Environments:

sandpit:
 -  Files located in sandpit are used for all non-ase resources: Pools, SNDIMPINFRGP001-static-test, SNDIMPINFRGP001-static-integration

 sandpitasev2:
 - Files here are used to deploy to vnet environment from jenkins: SNDIMPINFRGP001-static-test

 Octopus:
 - Octopus is used for all our route to live environments: SND, TST, PRE and PRD
 - The files in here must be updated to reflect any environment variable updated to route to live Environments

 *** Octopus Folder ***

 Until we can reconfigure templates and parameters to support Octopus, the Octopus folder in route of 'configuration/' is used.
 'configuration/octopus/templates' 'configuration/octopus/parameters'

 These templates will be required to be updated in parallel with sandpit and sandpitasev2 to reflect changes.
 It is recommend engaging with ops before making changes.

