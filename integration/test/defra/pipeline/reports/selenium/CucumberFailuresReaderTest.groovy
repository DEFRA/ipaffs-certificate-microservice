package defra.pipeline.reports.selenium

import defra.pipeline.reports.selenium.model.FailedCucumberStep
import defra.pipeline.reports.selenium.model.JenkinsBuildDetail
import defra.pipeline.reports.selenium.model.JenkinsProject
import org.junit.Test

import static org.junit.Assert.assertEquals

class CucumberFailuresReaderTest {

    private static final JenkinsProject PROJECT
    private static final JenkinsBuildDetail BUILD
    static {
        PROJECT = new JenkinsProject()
        PROJECT.url = "http://project.com"
        PROJECT.name = "my-project"
        BUILD = new JenkinsBuildDetail()
        BUILD.branch = "master"
        BUILD.url = "http://url.com"
        BUILD.number = 5
    }

    @Test
    void failuresFromCucumberJsonReport_beforeStepFailure_returnsFailure() {
        String json = new File("test/defra/pipeline/reports/selenium/resources/before_step_failure.json").text
        CucumberFailuresReader cucumberFailuresReader = new CucumberFailuresReader()
        List<FailedCucumberStep> failedSteps = cucumberFailuresReader.failuresFromCucumberJsonReport(json, BUILD)
        assertEquals(1, failedSteps.size())
        FailedCucumberStep step = failedSteps.get(0)
        assertEquals("Failed @Before step", step.error)
        assertEquals("HooksTagged.setRegressionScenarioName(Scenario)", step.step)
        assertEquals("Assess new CED notification and mark as accepted", step.scenario)
        assertEquals("Selenium Regression Test | Accept decision for CED", step.feature)
        assertEquals(BUILD, step.jenkinsBuild)
    }

    @Test
    void failuresFromCucumberJsonReport_afterStepFailure_returnsFailure() {
        String json = new File("test/defra/pipeline/reports/selenium/resources/after_step_failure.json").text
        CucumberFailuresReader cucumberFailuresReader = new CucumberFailuresReader()
        List<FailedCucumberStep> failedSteps = cucumberFailuresReader.failuresFromCucumberJsonReport(json, BUILD)
        assertEquals(1, failedSteps.size())
        FailedCucumberStep step = failedSteps.get(0)
        assertEquals("Failed @After step", step.error)
        assertEquals("Hooks.somethingThatDoesntWork(Scenario)", step.step)
        assertEquals("Assess new CED notification and mark as accepted", step.scenario)
        assertEquals("Selenium Regression Test | Accept decision for CED", step.feature)
        assertEquals(BUILD, step.jenkinsBuild)
    }

    @Test
    void failuresFromCucumberJsonReport_stepFailures_returnsFailures() {
        String json = new File("test/defra/pipeline/reports/selenium/resources/step_failures.json").text
        CucumberFailuresReader cucumberFailuresReader = new CucumberFailuresReader()
        List<FailedCucumberStep> failedSteps = cucumberFailuresReader.failuresFromCucumberJsonReport(json, BUILD)
        assertEquals(2, failedSteps.size())
        FailedCucumberStep step1 = failedSteps.get(0)
        assertEquals("Step failed", step1.error)
        assertEquals("the notification version number INVALID is displayed", step1.step)
        assertEquals("Official veterinarian assess notification and mark as destroyed", step1.scenario)
        assertEquals("Selenium Sanity Test | Official veterinarian checks", step1.feature)
        assertEquals(BUILD, step1.jenkinsBuild)

        FailedCucumberStep step2 = failedSteps.get(1)
        assertEquals("Another step failed", step2.error)
        assertEquals("I click save on control consignment leave page", step2.step)
        assertEquals("Scenario 2", step2.scenario)
        assertEquals("Selenium Sanity Test | Official veterinarian checks", step2.feature)
        assertEquals(BUILD, step2.jenkinsBuild)
    }

    @Test
    void failuresFromCucumberJsonReport_noStepFailures_returnsEmptyList() {
        String json = new File("test/defra/pipeline/reports/selenium/resources/no_step_failures.json").text
        CucumberFailuresReader cucumberFailuresReader = new CucumberFailuresReader()
        List<FailedCucumberStep> failedSteps = cucumberFailuresReader.failuresFromCucumberJsonReport(json, BUILD)
        assertEquals(0, failedSteps.size())
    }

}
