package defra.pipeline.reports.selenium

import com.fasterxml.jackson.databind.ObjectMapper
import defra.pipeline.reports.selenium.model.FailedCucumberStep
import defra.pipeline.reports.selenium.model.JenkinsBuild
import defra.pipeline.reports.selenium.model.JenkinsBuildDetail
import defra.pipeline.reports.selenium.model.JenkinsProject
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

class JenkinsClientTest {

    private static final JENKINS_URL = "http://jenkins.url"
    public static final JenkinsProject PROJECT = new JenkinsProject()
    public static final JenkinsBuildDetail BUILD = new JenkinsBuildDetail()
    static {
        PROJECT.url = "http://project.com/"
        PROJECT.name = "my-project"
        BUILD.branch = "master"
        BUILD.url = "http://url.com/"
        BUILD.number = 5
        BUILD.project = PROJECT
    }

    private final ObjectMapper objectMapper = new ObjectMapper()

    @Test(expected = IOException)
    void getProjectsWithSeleniumTests_exceptionReadingApi_throwsException() {
        def httpClient = [readBytes: {
            String url -> throw new IOException("error")
        }] as JenkinsHttpClient
        JenkinsClient jenkinsClient = new JenkinsClient(httpClient, null, null, JENKINS_URL)
        jenkinsClient.getProjectsWithSeleniumTests()
    }

    @Test
    void getProjectsWithSeleniumTests_validApiResponse_returnsProjects() {
        def actualUrl = null
        def httpClient = [readBytes: {
            String url -> actualUrl = url; return new File("test/defra/pipeline/reports/selenium/resources/jobs.json").bytes
        }] as JenkinsHttpClient
        JenkinsClient jenkinsClient = new JenkinsClient(httpClient, null, new ObjectMapper(), JENKINS_URL)
        List<JenkinsProject> projects = jenkinsClient.getProjectsWithSeleniumTests()
        assertEquals(3, projects.size())
        def actualNames = projects.collect {it.name}
        assertTrue(actualNames.containsAll(["my-microservice", "frontend-my", "permissions"]))
        assertEquals("my-microservice", projects.get(0).name)
        assertEquals("https://jenkins.url/job/my-microservice/", projects.get(0).url)
        assertEquals("frontend-my", projects.get(1).name)
        assertEquals("https://jenkins.url/job/frontend-my/", projects.get(1).url)
        assertEquals(JENKINS_URL+"/view/all/api/json", actualUrl)
    }

    @Test(expected = Exception.class)
    void getBuildsForJob_exceptionReadingApi_throwsException() {
        def actualUrl = null
        def httpClient = [
            readBytes: {
                String url ->
                    actualUrl = url
                    throw new Exception("error")
            }
        ] as JenkinsHttpClient
        JenkinsClient jenkinsClient = new JenkinsClient(httpClient, null, null, JENKINS_URL)
        JenkinsProject project = new JenkinsProject()
        project.url = "http://url.com/"
        project.name = "project-name"
        String branch = "branch-name"
        List<JenkinsBuild> result = jenkinsClient.getBuildsForJob(project, branch)
        assertEquals(0, result.size())
        assertEquals("http://url.com/job/branch-name/api/json", actualUrl)
    }

    @Test
    void getBuildsForJob_validApiResponse_returnsProjects() {
        def actualUrl = null
        def httpClient = [
            readBytes: {
                String url ->
                    actualUrl = url
                    return new File("test/defra/pipeline/reports/selenium/resources/job.json").bytes
            }
        ] as JenkinsHttpClient
        JenkinsClient jenkinsClient = new JenkinsClient(httpClient, null, new ObjectMapper(), JENKINS_URL)
        String branch = "branch-name"
        List<JenkinsBuild> builds = jenkinsClient.getBuildsForJob(PROJECT, branch)
        assertEquals(3, builds.size())
        JenkinsBuild build = builds.get(0)
        assertEquals("https://jenkins.url/job/my-project/job/master/136/", build.url)
        assertEquals(136, build.number)
        assertEquals("http://project.com/", build.project.url)
        assertEquals("my-project", build.project.name)
        assertEquals("branch-name", build.branch)
        assertEquals("http://project.com/job/branch-name/api/json", actualUrl)
    }

    @Test(expected = IOException.class)
    void getBuildDetails_exceptionReadingApi_throwsException() {
        def httpClient = [readBytes: {
            String url -> throw new IOException("error")
        }] as JenkinsHttpClient

        JenkinsClient jenkinsClient = new JenkinsClient(httpClient, null, null, JENKINS_URL)
        jenkinsClient.getBuildDetails([BUILD], 1234, 5678)
    }

    @Test
    void getBuildDetails_validApiResponses_returnsBuildStartedAfterSpecifiedTime() {
        final def AFTER_MAX_RANGE_URL = "http://test.jenkins.com/job/my-project/job/master/7/"
        final def INSIDE_RANGE_URL = "http://test.jenkins.com/job/my-project/job/master/6/"
        final def INSIDE_RANGE_PASSED_URL = "http://test.jenkins.com/job/my-project/job/master/5/"
        final def BEFORE_MIN_RANGE_1_URL = "http://test.jenkins.com/job/my-project/job/master/4/"
        final def BEFORE_MIN_RANGE_2_URL = "http://test.jenkins.com/job/my-project/job/master/3/"

        final def MIN = 1556292500000
        final def MAX = 1556292600000
        final def OFF = 50000

        JenkinsBuild buildTemplate = new JenkinsBuild()
        buildTemplate.branch = "master"
        buildTemplate.project = PROJECT

        JenkinsBuild afterMaxRange = clone(buildTemplate)
        afterMaxRange.url = AFTER_MAX_RANGE_URL
        afterMaxRange.number = 7

        JenkinsBuild withinRange = clone(buildTemplate)
        withinRange.url = INSIDE_RANGE_URL
        withinRange.number = 6

        JenkinsBuild withinRangePassed = clone(buildTemplate)
        withinRangePassed.url = INSIDE_RANGE_PASSED_URL
        withinRangePassed.number = 5

        JenkinsBuild beforeMinRange1 = clone(buildTemplate)
        beforeMinRange1.url = BEFORE_MIN_RANGE_1_URL
        beforeMinRange1.number = 4

        JenkinsBuild beforeMinRange2 = clone(buildTemplate)
        beforeMinRange2.url = BEFORE_MIN_RANGE_2_URL
        beforeMinRange2.number = 3

        def allBuilds = [afterMaxRange, withinRange, withinRangePassed, beforeMinRange1, beforeMinRange2]

        def actualUrls = []
        def httpClient = [readBytes: {
            String url ->
                actualUrls.add(url)
                if (url.contains(AFTER_MAX_RANGE_URL)) {
                    return buildDetails(MAX + OFF)
                } else if (url.contains(INSIDE_RANGE_URL)) {
                    return buildDetails(MAX - OFF)
                } else if (url.contains(INSIDE_RANGE_PASSED_URL)) {
                    return buildDetails(MAX - OFF, "PASSED")
                } else if (url.contains(BEFORE_MIN_RANGE_1_URL)) {
                    return buildDetails(MIN - OFF)
                } else if (url.contains(BEFORE_MIN_RANGE_2_URL)) {
                    return buildDetails(MAX - OFF - OFF)
                }
                throw new IllegalArgumentException("Unexpected url: ${url}")
        }] as JenkinsHttpClient

        JenkinsClient jenkinsClient = new JenkinsClient(httpClient, null, new ObjectMapper(), JENKINS_URL)

        List<JenkinsBuild> builds = jenkinsClient.getBuildDetails(allBuilds, MIN, MAX)
        assertEquals(1, builds.size())
        JenkinsBuild build = builds.get(0)
        assertEquals(INSIDE_RANGE_URL, build.url)
        assertEquals(6, build.number)
        assertEquals("http://project.com/", build.project.url)
        assertEquals("my-project", build.project.name)
        assertEquals("master", build.branch)
        assertEquals(MAX - OFF, build.timestamp)
        def expectedUrls = [afterMaxRange, withinRange, withinRangePassed, beforeMinRange1].collect({it.url + "api/json"})
        assertEquals(expectedUrls.size(), actualUrls.size())
        assertTrue(actualUrls.containsAll(expectedUrls))
    }

    private byte[] buildDetails(long timestamp, String result = "FAILURE") {
        return ('{"timestamp":' + timestamp + ', "result":"' + result + '"}').getBytes("UTF-8")
    }

    private JenkinsBuild clone(JenkinsBuild build) {
        return objectMapper.readValue(objectMapper.writeValueAsString(build), JenkinsBuild.class)
    }

    @Test
    void getFailuresForBuild_http403Error_returnsEmptyList() {
        def actualUrl = null
        def httpClient = [
            readBytes: {
                String url ->
                    actualUrl = url
                    throw new JenkinsHttpClientException(403, "error")
            }
        ] as JenkinsHttpClient

        JenkinsClient jenkinsClient = new JenkinsClient(httpClient, null, null, JENKINS_URL)
        List<FailedCucumberStep> result = jenkinsClient.getFailuresForBuild(BUILD)
        assertEquals(0, result.size())
        assertEquals("http://url.com/artifact/*zip*/archive.zip", actualUrl)
    }

    @Test(expected = JenkinsHttpClientException)
    void getFailuresForBuild_http401Error_throwsException() {
        def actualUrl = null
        def httpClient = [
                readBytes: {
                    String url ->
                        actualUrl = url
                        throw new JenkinsHttpClientException(401, "error")
                }
        ] as JenkinsHttpClient

        JenkinsClient jenkinsClient = new JenkinsClient(httpClient, null, null, JENKINS_URL)
        jenkinsClient.getFailuresForBuild(BUILD)
    }

    @Test
    void getFailuresForBuild_validApiResponses_returnsFailedSteps() {
        def actualUrl = null
        def httpClient = [
            readBytes: {
                String url ->
                    actualUrl = url
                    new byte[0]
            }
        ] as JenkinsHttpClient

        def archiveReader = [
            failuresFromCucumberReportsArchive: {
                ByteArrayInputStream bais, JenkinsBuild b ->
                    FailedCucumberStep step = new FailedCucumberStep()
                    step.step = "step"
                    step.scenario = "scenario"
                    step.feature = "feature"
                    step.error = "error"
                    step.jenkinsBuild = BUILD
                    return [step]
            }
        ] as ArtifactArchiveReader

        JenkinsClient jenkinsClient = new JenkinsClient(httpClient, archiveReader, new ObjectMapper(), JENKINS_URL)
        List<FailedCucumberStep> result = jenkinsClient.getFailuresForBuild(BUILD)
        assertEquals(1, result.size())
        assertEquals("http://url.com/artifact/*zip*/archive.zip", actualUrl)
    }

}
