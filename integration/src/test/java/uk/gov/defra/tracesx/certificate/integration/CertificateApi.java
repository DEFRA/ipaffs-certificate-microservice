package uk.gov.defra.tracesx.certificate.integration;

import static io.restassured.RestAssured.given;
import static uk.gov.defra.tracesx.integration.certificate.helpers.JwtConstants.BEARER;

import io.restassured.response.Response;
import uk.gov.defra.tracesx.common.security.tests.jwt.SelfSignedTokenClient;
import uk.gov.defra.tracesx.common.security.tests.jwt.SelfSignedTokenClient.TokenType;


public class CertificateApi {

  private TestEnvironment testEnvironment;

  private static final String AUTHORIZATION = "Authorization";

  private final SelfSignedTokenClient tokenClient = new SelfSignedTokenClient();

  public CertificateApi(TestEnvironment testEnvironment) {
    this.testEnvironment = testEnvironment;
  }

  public Response getPdf(String htmlContent, String reference, String url) {


    Response response = given()
        .header(AUTHORIZATION, BEARER + tokenClient.getToken(TokenType.AD))
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
