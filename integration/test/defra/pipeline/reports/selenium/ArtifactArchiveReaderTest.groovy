package defra.pipeline.reports.selenium

import defra.pipeline.reports.selenium.model.FailedCucumberStep
import defra.pipeline.reports.selenium.model.JenkinsBuild
import defra.pipeline.reports.selenium.model.JenkinsBuildDetail
import org.junit.Test

import static org.junit.Assert.assertEquals

class ArtifactArchiveReaderTest {

    @Test
    void failuresFromCucumberReportsArchive_withThreeJsonFilesInZip_callsReaderThreeTimes() {
        def callCount = 0
        CucumberFailuresReader cucumberFailuresReader = [
                failuresFromCucumberJsonReport: { String content, JenkinsBuild b -> [createStepFailure(++callCount)]}
        ] as CucumberFailuresReader

        def build = [] as JenkinsBuildDetail

        ArtifactArchiveReader archiveReader = new ArtifactArchiveReader(cucumberFailuresReader)
        new File("test/defra/pipeline/reports/selenium/resources/artifact_archive.zip").withDataInputStream {
            List<FailedCucumberStep> result = archiveReader.failuresFromCucumberReportsArchive(it, build)
            assertEquals(3, result.size())
            assertEquals("1", result.get(0).step)
            assertEquals("2", result.get(1).step)
            assertEquals("3", result.get(2).step)
        }
    }

    @Test
    void failuresFromCucumberReportsArchive_withEmptyJson_readerNotCalled() {
        def callCount = 0
        CucumberFailuresReader cucumberFailuresReader = [
                failuresFromCucumberJsonReport: { String content, JenkinsBuild b -> [createStepFailure(++callCount)]}
        ] as CucumberFailuresReader

        def build = [] as JenkinsBuildDetail

        ArtifactArchiveReader archiveReader = new ArtifactArchiveReader(cucumberFailuresReader)
        new File("test/defra/pipeline/reports/selenium/resources/artifact_archive_empty_json.zip").withDataInputStream {
            List<FailedCucumberStep> result = archiveReader.failuresFromCucumberReportsArchive(it, build)
            assertEquals(3, result.size())
        }
    }

    @Test
    void failuresFromCucumberReportsArchive_withUnrecognisedFileNames_readerNotCalled() {
        def callCount = 0
        CucumberFailuresReader cucumberFailuresReader = [
                failuresFromCucumberJsonReport: { String content, JenkinsBuild b -> [createStepFailure(++callCount)]}
        ] as CucumberFailuresReader

        def build = [] as JenkinsBuildDetail

        ArtifactArchiveReader archiveReader = new ArtifactArchiveReader(cucumberFailuresReader)
        new File("test/defra/pipeline/reports/selenium/resources/artifact_archive_unrecognised_file_names.zip").withDataInputStream {
            List<FailedCucumberStep> result = archiveReader.failuresFromCucumberReportsArchive(it, build)
            assertEquals(0, result.size())
        }
    }

    private static FailedCucumberStep createStepFailure(int count) {
        FailedCucumberStep step = new FailedCucumberStep()
        step.step = "" + count
        return step
    }
}
