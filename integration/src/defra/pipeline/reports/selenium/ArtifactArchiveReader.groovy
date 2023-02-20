package defra.pipeline.reports.selenium

import defra.pipeline.reports.selenium.model.FailedCucumberStep
import defra.pipeline.reports.selenium.model.JenkinsBuildDetail
import org.apache.commons.io.IOUtils

import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class ArtifactArchiveReader {

    private static final Pattern CUCUMBER_REPORT_FILENAME = Pattern.compile(".*\\d+.json")

    private final CucumberFailuresReader cucumberFailuresReader

    ArtifactArchiveReader() {
        // for unit test mocking only
    }

    ArtifactArchiveReader(CucumberFailuresReader cucumberFailuresReader) {
        this.cucumberFailuresReader = cucumberFailuresReader
    }

    List<FailedCucumberStep> failuresFromCucumberReportsArchive(InputStream zipFileInputStream, JenkinsBuildDetail build) {
        List<FailedCucumberStep> failedSteps = new ArrayList()
        ZipInputStream zipInputStream = new ZipInputStream(zipFileInputStream)
        try {
            ZipEntry zipEntry
            while (zipEntry = zipInputStream.getNextEntry()) {
                if(isValidReportFile(zipEntry)) {
                    String json = IOUtils.toString(zipInputStream)
                    if (json != null && !json.isEmpty()) {
                        List<FailedCucumberStep> failuresInReport = cucumberFailuresReader.failuresFromCucumberJsonReport(json, build)
                        failedSteps.addAll(failuresInReport)
                    } else {
                        failedSteps.add(createForEmptyReport(zipEntry.name, build))
                    }
                }
                zipInputStream.closeEntry()
            }
        } finally {
            zipInputStream.close()
            zipFileInputStream.close()
        }
        return failedSteps
    }

    private static boolean isValidReportFile(ZipEntry zipEntry) {
        return !zipEntry.isDirectory() && zipEntry.getName().matches(CUCUMBER_REPORT_FILENAME)
    }

    private static FailedCucumberStep createForEmptyReport(String fileName, JenkinsBuildDetail build) {
        FailedCucumberStep step = new FailedCucumberStep()
        step.jenkinsBuild = build
        step.error = "The cucumber JSON report file was empty"
        step.scenario = "n/a"
        step.feature = fileName
        step.step = "n/a"
        return step
    }

}
