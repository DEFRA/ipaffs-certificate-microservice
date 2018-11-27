package uk.gov.defra.tracesx.certificate.integration;

public class TestEnvironment {

  private final String username;
  private final String password;
  private final String url;

  public TestEnvironment(String username, String password, String url) {
    this.username = username;
    this.password = password;
    this.url = url;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getBaseUrl() {
    return url;
  }
}
