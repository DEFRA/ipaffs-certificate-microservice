# Selenium Failures Report

The report will aggregate selenium test failures across all microservices and frontends over a specified period of time.

Each failure is written out to a CSV file.

## Running Locally

1) In the Jenkins UI click your username in the upper-right hand corner
2) Click 'Configure'
3) In the API Token section, click 'Add new Token'
4) Set the environment variable `JENKINS_TOKEN` to the generated token value
5) Set the environment variable `JENKINS_USER` to the username you login to Jenkins with
6) Run `defra.pipeline.reports.selenium.SeleniumFailureReport` 

A command-line argument is required to specify the start date of the report in format `dd/MM/yyyy HH:mm:ss`

From the command line: `./gradlew runSeleniumReport -Dexec.args="30/04/2019 00:00:00,01/05/2019 00:00:00"`

The report will be saved to `report.csv` in the current working directory.

## Running in a Pipeline

1) Store service user credentials in the keyvault
2) Use the `vars/seleniumFailureReport.groovy` function in a `Jenkinsfile` / pipeline script
3) The report is saved as `selenium_failures-{startDate}-{endDate}.csv`. Ensure the pipeline archives `archiveArtifacts: *.csv`
