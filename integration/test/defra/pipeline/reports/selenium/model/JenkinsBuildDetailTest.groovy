package defra.pipeline.reports.selenium.model

import org.junit.Test

import static org.junit.Assert.assertEquals

class JenkinsBuildDetailTest {

    @Test
    void constructor_copiesSuperclassFields() {
        JenkinsProject project = new JenkinsProject()
        project.url = "url/project"
        project.name = "project"

        JenkinsBuild original = new JenkinsBuild()
        original.url = "url/build"
        original.number = 5
        original.branch = "branch"
        original.project = project

        JenkinsBuildDetail detail = new JenkinsBuildDetail(original)
        assertEquals(original.url, detail.url)
        assertEquals(original.number, detail.number)
        assertEquals(original.branch, detail.branch)
        assertEquals(original.project, detail.project)
    }
}
