package defra.pipeline.reports.selenium

import com.fasterxml.jackson.databind.ObjectMapper
import defra.pipeline.reports.selenium.model.FailedCucumberStep
import defra.pipeline.reports.selenium.model.JenkinsBuildDetail

class CucumberFailuresReader {

    List<FailedCucumberStep> failuresFromCucumberJsonReport(String content, JenkinsBuildDetail build) {
        def objectMapper = new ObjectMapper()
        def rootArray = objectMapper.readValue(content, List.class)
        List<?> failures = rootArray.collect {readFeature(it, build)}
        failures = failures.flatten()
        failures.removeAll([null])
        return failures
    }

    private List<FailedCucumberStep> readFeature(def feature, JenkinsBuildDetail build) {
        final String featureName = feature.name
        return feature.elements.collect {readScenario(it, featureName, build)}
    }

    private FailedCucumberStep readScenario(def scenario, String featureName, JenkinsBuildDetail build) {
        def steps = emptyIfNull(scenario.before) + emptyIfNull(scenario.steps) + emptyIfNull(scenario.after)
        for (def step in steps) {
            def failedStep = createStepIfFailed(step, featureName, scenario.name, build)
            if(failedStep) {
                return failedStep
            }
        }
    }

    private def emptyIfNull(def list) {
        return list != null ? list : []
    }

    private FailedCucumberStep createStepIfFailed(def stepObj, String featureName, String scenarioName, JenkinsBuildDetail build) {
        if(stepObj.result.status != "passed") {
            FailedCucumberStep step = new FailedCucumberStep()
            step.feature = featureName
            step.scenario = scenarioName
            step.step = stepObj.name ? stepObj.name : stepObj.match.location
            step.error = stepObj.result.error_message
            step.jenkinsBuild = build
            return step
        }
    }

}
