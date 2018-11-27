package uk.gov.defra.tracesx.certificate.integration;

import static io.restassured.RestAssured.given;

import io.restassured.response.Response;

public class CertificateApi {

  private TestEnvironment testEnvironment;

  public CertificateApi(TestEnvironment testEnvironment) {
    this.testEnvironment = testEnvironment;
  }

  public Response getPdf(String htmlContent, String reference, String url) {
    Response response = given()
        .auth()
        .basic(testEnvironment.getUsername(), testEnvironment.getPassword())
        .body(htmlContent)
        .when()
        .post(getUrl("/certificate/", reference, url))
        .andReturn();
    return response;
  }

  private String getUrl(String path, String reference, String url) {
    return testEnvironment.getBaseUrl() + path + reference + "?url=" + url;
  }

}
