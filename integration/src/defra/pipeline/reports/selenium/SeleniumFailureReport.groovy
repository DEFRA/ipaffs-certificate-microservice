package defra.pipeline.reports.selenium

import com.fasterxml.jackson.databind.ObjectMapper
import defra.pipeline.reports.selenium.model.FailedCucumberStep
import defra.pipeline.reports.selenium.model.JenkinsBuild
import defra.pipeline.reports.selenium.model.JenkinsBuildDetail
import defra.pipeline.reports.selenium.model.JenkinsProject
import org.apache.commons.lang3.StringEscapeUtils

import java.text.DateFormat
import java.text.SimpleDateFormat

class SeleniumFailureReport {

    private static def LINE_SEPARATOR = System.getProperty("line.separator")
    private static final DateFormat TS_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    private final JenkinsClient jenkinsClient

    SeleniumFailureReport(JenkinsClient jenkinsClient) {
        this.jenkinsClient = jenkinsClient
    }

    List<FailedCucumberStep> runReport(String branch, Date fromDate, Date toDate) {
        List<JenkinsProject> projects = jenkinsClient.getProjectsWithSeleniumTests()
        def builds = projects.collect { allBuildsAfter(it, branch, fromDate, toDate) }
        builds = flattenAndRemoveNull(builds)
        def failures = builds.collect { jenkinsClient.getFailuresForBuild(it) }
        return flattenAndRemoveNull(failures)
    }

    void writeAsCsv(File file, List<FailedCucumberStep> failures) {
        PrintWriter writer = file.newPrintWriter()
        writer.write("project,branch,build_num,build_url,build_timestamp,feature,scenario,step,error"+LINE_SEPARATOR)
        failures.each {
            def branch = urlDecodeJenkinsMultipleEncoding(it.jenkinsBuild.branch)
            def url = StringEscapeUtils.escapeCsv(it.jenkinsBuild.url)
            def timestamp = TS_FORMAT.format(new Date(it.jenkinsBuild.timestamp))
            def feature = StringEscapeUtils.escapeCsv(it.feature)
            def scenario = StringEscapeUtils.escapeCsv(it.scenario)
            def step = StringEscapeUtils.escapeCsv(it.step)
            def errorMsg = StringEscapeUtils.escapeCsv(it.error)
            writer.write("${it.jenkinsBuild.project.name},${branch},${it.jenkinsBuild.number},${url},${timestamp},${feature},${scenario},${step},${errorMsg}"+LINE_SEPARATOR)
        }
        writer.close()
    }

    private static urlDecodeJenkinsMultipleEncoding(def str) {
        while(str.contains("%"))
            str = URLDecoder.decode(str, "UTF-8")
        return str
    }

    private List<JenkinsBuildDetail> allBuildsAfter(JenkinsProject project, String branch, Date fromDate, Date toDate) {
        List<JenkinsBuild> builds = jenkinsClient.getBuildsForJob(project, branch)
        long from = fromDate.toInstant().toEpochMilli()
        long to = toDate.toInstant().toEpochMilli()
        return jenkinsClient.getBuildDetails(builds, from, to)
    }

    private static <T> List<T> flattenAndRemoveNull(List<T> list) {
        list = list.flatten()
        list.removeAll([null])
        return list
    }

    static void main(String[] args) {
        String jenkinsUser = System.getenv("JENKINS_USER")
        String jenkinsToken = System.getenv("JENKINS_TOKEN")
        String jenkinsUrl = "https://jenkins-imports.azure.defra.cloud"
        JenkinsHttpClient httpClient = new JenkinsHttpClient(jenkinsUser, jenkinsToken)
        CucumberFailuresReader cucumberFailuresReader = new CucumberFailuresReader()
        ArtifactArchiveReader artifactArchiveReader = new ArtifactArchiveReader(cucumberFailuresReader)
        ObjectMapper objectMapper = new ObjectMapper()
        JenkinsClient jenkinsClient = new JenkinsClient(httpClient, artifactArchiveReader, objectMapper, jenkinsUrl)
        SeleniumFailureReport seleniumFailureReport = new SeleniumFailureReport(jenkinsClient)

        final def format = 'dd/MM/yyyy HH:mm:ss'
        String fromString = args[0]
        String toString = args[1]
        Date from = Date.parse(format, fromString)
        Date to = Date.parse(format, toString)
        def failures = seleniumFailureReport.runReport("master", from, to)
        seleniumFailureReport.writeAsCsv(new File("report.csv"), failures)
    }

}
