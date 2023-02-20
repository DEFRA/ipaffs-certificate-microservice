package defra.pipeline.reports.selenium

class JenkinsHttpClientException extends Exception {

    final int status

    JenkinsHttpClientException(int status, String message) {
        super(message)
        this.status = status
    }

}
