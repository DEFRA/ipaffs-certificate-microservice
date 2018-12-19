package uk.gov.defra.tracesx.certificate.integration;
import static io.restassured.RestAssured.given;
import static java.nio.charset.StandardCharsets.UTF_8;
import static uk.gov.defra.tracesx.certificate.integration.TestApiAuthentication.AUTHORIZATION;
import static uk.gov.defra.tracesx.integration.certificate.helpers.JwtConstants.BEARER;
import static uk.gov.defra.tracesx.integration.certificate.helpers.JwtConstants.ROLES;

import io.restassured.response.Response;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import uk.gov.defra.tracesx.integration.certificate.helpers.TokenHelper;

public class CertificateApi {

  private TestEnvironment testEnvironment;

  public static final String COLON = ":";
  public static final String BASIC = "Basic ";
  private String encodedBasicAuth;

  public CertificateApi(TestEnvironment testEnvironment) {
    this.testEnvironment = testEnvironment;

  }

  public Response getPdf(String htmlContent, String reference, String url) throws UnsupportedEncodingException {

    encodedBasicAuth = BASIC + Base64.getEncoder().encodeToString(
            new StringBuffer().append(testEnvironment.getUsername()).append(COLON).append(testEnvironment.getPassword()).toString().getBytes(
                    UTF_8.name()));

    Response response = given().header("x-auth-basic",encodedBasicAuth).header(AUTHORIZATION, BEARER + TokenHelper
        .getValidToken(ROLES))
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
