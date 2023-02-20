import com.fasterxml.jackson.databind.ObjectMapper
import defra.pipeline.reports.selenium.ArtifactArchiveReader
import defra.pipeline.reports.selenium.CucumberFailuresReader
import defra.pipeline.reports.selenium.JenkinsClient
import defra.pipeline.reports.selenium.JenkinsHttpClient
import defra.pipeline.reports.selenium.SeleniumFailureReport
import defra.pipeline.reports.selenium.model.FailedCucumberStep
import defra.pipeline.vault.VaultKey
import org.apache.commons.lang.StringUtils

import java.text.SimpleDateFormat

def call(String start, String end, String branch = 'master') {
    Date startDate = parseStart(start)
    Date endDate = parseEnd(end)
    String filename = String.format("selenium_failures-%s-%s.csv", trimDate(start), trimDate(end))
    echo("Generating selenium failure report for branch ${branch} from ${start} until ${end} into file ${filename}")
    SeleniumFailureReport seleniumFailureReport = createReportGenerator()
    List<FailedCucumberStep> failures = seleniumFailureReport.runReport(branch, startDate, endDate)
    final File file = new File(filename)
    seleniumFailureReport.writeAsCsv(file, failures)
}

String trimDate(String dateString) {
    return dateString
            .replace('-', '')
            .replace(':', '')
            .replace(' ', '')
            .trim()
}

Date parseStart(String start) {
    if(StringUtils.isNotBlank(start)) {
        return parseDate(start)
    }
    Calendar calendar = new GregorianCalendar()
    calendar.add(Calendar.DAY_OF_YEAR, -1)
    return new Date(calendar.toInstant().toEpochMilli())
}

Date parseEnd(String end) {
    if(StringUtils.isNotBlank(end)) {
        return parseDate(end)
    }
    return new Date()
}

Date parseDate(String date) {
    final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    return DATE_FORMAT.parse(date.trim())
}

SeleniumFailureReport createReportGenerator() {
    String jenkinsUser = 'SeleniumReportSystemUser'
    String jenkinsToken = VaultKey.getSecuredValue('SeleniumReportSystemUserPassword', this)
    JenkinsHttpClient httpClient = new JenkinsHttpClient(jenkinsUser, jenkinsToken)
    CucumberFailuresReader cucumberFailuresReader = new CucumberFailuresReader()
    ArtifactArchiveReader artifactArchiveReader = new ArtifactArchiveReader(cucumberFailuresReader)
    ObjectMapper objectMapper = new ObjectMapper()
    JenkinsClient jenkinsClient = new JenkinsClient(httpClient, artifactArchiveReader, objectMapper)
    return new SeleniumFailureReport(jenkinsClient)
}
