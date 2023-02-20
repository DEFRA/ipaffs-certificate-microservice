package defra.pipeline.reports.selenium

import defra.pipeline.reports.selenium.model.FailedCucumberStep
import defra.pipeline.reports.selenium.model.JenkinsBuild
import defra.pipeline.reports.selenium.model.JenkinsBuildDetail
import defra.pipeline.reports.selenium.model.JenkinsProject
import org.junit.Test

import static org.junit.Assert.assertEquals

class SeleniumFailureReportTest {

    private static def LINE_SEPARATOR = System.getProperty("line.separator")
    private static final long TS = System.currentTimeMillis()
    private static final String TS_DATE = new Date(TS).format("yyyy-MM-dd HH:mm:ss")
    private static final String BRANCH_ENCODED = "feature%252Fbranch"
    private static final String BRANCH_DECODED = "feature/branch"
    private static final JenkinsProject PROJECT
    private static final JenkinsBuildDetail BUILD
    static {
        PROJECT = new JenkinsProject()
        PROJECT.url = "http://project.com"
        PROJECT.name = "my-project"
        BUILD = new JenkinsBuildDetail()
        BUILD.branch = BRANCH_ENCODED
        BUILD.url = "http://url.com"
        BUILD.number = 5
        BUILD.project = PROJECT
        BUILD.timestamp = TS
    }

    @Test
    void runReport_noClientErrors_returnsListOfFailures() {
        JenkinsProject project1 = new JenkinsProject()
        project1.name = "project1"
        project1.url = "http://project1.url"
        JenkinsProject project2 = new JenkinsProject()
        project1.name = "project2"
        project1.url = "http://project2.url"

        def build = 1
        def actualFrom = null
        def actualTo = null
        JenkinsClient jenkinsClient = [
            getProjectsWithSeleniumTests: {
                return [project1, project2]
            },
            getBuildsForJob: { JenkinsProject project, String branch ->
                JenkinsBuild b = new JenkinsBuild()
                b.url = "${project.url}/build"
                b.number = build++
                b.branch = branch
                b.project = project
                return [b]
            },
            getBuildDetails: {List<JenkinsBuild> builds, long from, long to ->
                actualFrom = from
                actualTo = to
                return builds.collect {
                    JenkinsBuildDetail detail = new JenkinsBuildDetail(it)
                    detail.timestamp = (to - from) / 2
                    detail.result = "PASSED"
                    return detail
                }
            },
            getFailuresForBuild: { JenkinsBuildDetail b ->
                FailedCucumberStep step = new FailedCucumberStep()
                step.step = "step${b.number}"
                step.jenkinsBuild = b
                step.error = "error${b.number}"
                step.feature = "feature${b.number}"
                step.scenario = "scenario${b.number}"
                return [step]
            }
        ] as JenkinsClient

        SeleniumFailureReport report = new SeleniumFailureReport(jenkinsClient)
        Long now = System.currentTimeMillis()
        Date from = new Date(now - 10000)
        Date to = new Date(now)
        List<FailedCucumberStep> steps = report.runReport(BRANCH_ENCODED, from, to)
        assertEquals(2, steps.size())
        FailedCucumberStep step1 = steps.get(0)
        assertEquals("step1", step1.step)
        assertEquals(1, step1.jenkinsBuild.number)
        assertEquals("feature1", step1.feature)
        assertEquals("scenario1", step1.scenario)
        assertEquals("error1", step1.error)
        FailedCucumberStep step2 = steps.get(1)
        assertEquals("step2", step2.step)
        assertEquals(2, step2.jenkinsBuild.number)
        assertEquals("feature2", step2.feature)
        assertEquals("scenario2", step2.scenario)
        assertEquals("error2", step2.error)

        assertEquals(from.toInstant().toEpochMilli(), actualFrom)
        assertEquals(to.toInstant().toEpochMilli(), actualTo)
    }

    @Test
    void writeAsCsv_withoutFailures_writesFile() {
        File out = File.createTempFile("writeAsCsv_withoutFailures_writesFile", "" + System.currentTimeMillis())
        out.deleteOnExit()
        System.out.println(out)
        SeleniumFailureReport report = new SeleniumFailureReport(null)
        report.writeAsCsv(out, [])
        String content = out.text
        assertEquals("project,branch,build_num,build_url,build_timestamp,feature,scenario,step,error" + LINE_SEPARATOR, content)

        new Date().minus(1).format("yyyy-MM-dd 00:00:00")
    }

    @Test
    void writeAsCsv_withFailures_writesFile() {
        FailedCucumberStep f1 = new FailedCucumberStep()
        f1.step = "step1"
        f1.jenkinsBuild = BUILD
        f1.error = "error1"
        f1.feature = "feature1"
        f1.scenario = "scenario1"
        FailedCucumberStep f2 = new FailedCucumberStep()
        f2.step = "step2"
        f2.jenkinsBuild = BUILD
        f2.error = "error2"
        f2.feature = "feature2"
        f2.scenario = "scenario2"
        def failures = [f1, f2]
        File out = File.createTempFile("writeAsCsv_withoutFailures_writesFile", "" + System.currentTimeMillis())
        out.deleteOnExit()
        SeleniumFailureReport report = new SeleniumFailureReport(null)
        report.writeAsCsv(out, failures)
        String[] content = out.text.split(LINE_SEPARATOR)
        assertEquals(3, content.length)
        assertEquals("project,branch,build_num,build_url,build_timestamp,feature,scenario,step,error", content[0])
        assertEquals("my-project,"+BRANCH_DECODED+",5,http://url.com,"+TS_DATE+",feature1,scenario1,step1,error1", content[1])
        assertEquals("my-project,"+BRANCH_DECODED+",5,http://url.com,"+TS_DATE+",feature2,scenario2,step2,error2", content[2])

    }

}
