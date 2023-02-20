package defra.pipeline.reports.selenium

import com.fasterxml.jackson.databind.ObjectMapper
import defra.pipeline.reports.selenium.model.FailedCucumberStep
import defra.pipeline.reports.selenium.model.JenkinsBuild
import defra.pipeline.reports.selenium.model.JenkinsBuildDetail
import defra.pipeline.reports.selenium.model.JenkinsProject

class JenkinsClient {

    private static final def IGNORE = ['openid-token-microservice', 'frontend-auth']

    private final JenkinsHttpClient httpClient
    private final ArtifactArchiveReader archiveReader
    private final ObjectMapper objectMapper
    private final String jenkinsUrl

    JenkinsClient(JenkinsHttpClient httpClient, ArtifactArchiveReader archiveReader,
                  ObjectMapper objectMapper, String jenkinsUrl) {
        this.httpClient = httpClient
        this.archiveReader = archiveReader
        this.objectMapper = objectMapper
        this.jenkinsUrl = jenkinsUrl
    }

    JenkinsClient() {
        // for unit test mocks
    }

    List<JenkinsProject> getProjectsWithSeleniumTests() {
        byte[] jobsContent = httpClient.readBytes("${jenkinsUrl}/view/all/api/json")
        def jobsResponse = new ObjectMapper().readValue(jobsContent, Map.class)
        List<JenkinsProject> jobs = jobsResponse.jobs.collect({
            JenkinsProject p = new JenkinsProject()
            p.name = it.name
            p.url = it.url
            return p
        })
        return jobs.findAll({isProjectWithSelenium(it.name)})
    }

    List<JenkinsBuild> getBuildsForJob(JenkinsProject project, String branch) {
        def jobResponse = httpClient.readBytes("${project.url}job/${branch}/api/json")
        def jobJson = objectMapper.readValue(jobResponse, Map.class)
        return jobJson.builds.collect {
            JenkinsBuild b = new JenkinsBuild()
            b.project = project
            b.branch = branch
            b.url = it.url
            b.number = it.number
            return b
        }
    }

    private JenkinsBuildDetail getBuildDetail(JenkinsBuild build) {
        def buildResponse = httpClient.readBytes("${build.url}api/json")
        def buildJson = objectMapper.readValue(buildResponse, Map.class)
        JenkinsBuildDetail detail = new JenkinsBuildDetail(build)
        detail.result = buildJson.result
        detail.timestamp = buildJson.timestamp
        return detail
    }

    List<JenkinsBuildDetail> getBuildDetails(List<JenkinsBuild> builds, long before, long end) {
        def details = []
        for (JenkinsBuild build in builds) {
            def detail = getBuildDetail(build)

            if (detail.timestamp > end) {
                continue
            }

            if (detail.timestamp < before) {
                break
            }

            if (isFailed(detail)) {
                details.add(detail)
            }
        }
        return details
    }

    private boolean isFailed(JenkinsBuildDetail build) {
        return build.result == "FAILURE"
    }

    List<FailedCucumberStep> getFailuresForBuild(JenkinsBuildDetail build) {
        try {
            byte[] zipFileContent = httpClient.readBytes("${build.url}artifact/*zip*/archive.zip")
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zipFileContent)
            return archiveReader.failuresFromCucumberReportsArchive(byteArrayInputStream, build)
        } catch (JenkinsHttpClientException e) {
            if(e.status == 403) {
                println "No reports were archived for ${build.project.name} ${build.branch} #${build.number}"
                return []
            } else {
                throw e
            }
        }
    }

    private static boolean isProjectWithSelenium(String name) {
        return !IGNORE.contains(name) &&
                (name.startsWith("frontend-") ||
                        name.endsWith("-microservice") ||
                        name == "permissions")
    }

}
