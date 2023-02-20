package defra.pipeline.reports.selenium.model

class FailedCucumberStep {
    String feature
    String scenario
    String step
    String error
    JenkinsBuildDetail jenkinsBuild
}
