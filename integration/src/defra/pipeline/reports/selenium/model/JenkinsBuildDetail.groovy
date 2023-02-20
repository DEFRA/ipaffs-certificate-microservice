package defra.pipeline.reports.selenium.model

class JenkinsBuildDetail extends JenkinsBuild {
    String result
    Long timestamp

    JenkinsBuildDetail() {

    }

    JenkinsBuildDetail(JenkinsBuild jenkinsBuild) {
        this.project = jenkinsBuild.project
        this.branch = jenkinsBuild.branch
        this.number = jenkinsBuild.number
        this.url = jenkinsBuild.url
    }
}
