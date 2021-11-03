package uk.gov.defra.tracesx.certificate.integration;

public class TestEnvironment {

  private final String url;

  public TestEnvironment(String url) {
    this.url = url;
  }

  public String getBaseUrl() {
    return url;
  }
}
