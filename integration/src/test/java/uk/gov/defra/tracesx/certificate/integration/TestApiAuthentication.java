package uk.gov.defra.tracesx.certificate.integration;

import static io.restassured.RestAssured.given;
import static uk.gov.defra.tracesx.integration.certificate.helpers.CertificateServiceHelper.X_AUTH_BASIC;
import static uk.gov.defra.tracesx.integration.certificate.helpers.JwtConstants.AUD;
import static uk.gov.defra.tracesx.integration.certificate.helpers.JwtConstants.BEARER;
import static uk.gov.defra.tracesx.integration.certificate.helpers.JwtConstants.ISS;
import static uk.gov.defra.tracesx.integration.certificate.helpers.SelfSignedTokenClient.TokenType.AD;
import static uk.gov.defra.tracesx.integration.certificate.helpers.SelfSignedTokenClient.TokenType.B2C;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.Collections;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import uk.gov.defra.tracesx.integration.certificate.helpers.CertificateServiceHelper;
import uk.gov.defra.tracesx.integration.certificate.helpers.SelfSignedTokenClient;
import uk.gov.defra.tracesx.integration.certificate.helpers.SelfSignedTokenClient.TokenType;
import uk.gov.defra.tracesx.integration.certificate.models.certificate.Certificate;

@RunWith(Theories.class)
public class TestApiAuthentication {

  private static final CertificateServiceHelper helper = new CertificateServiceHelper();

  private static final String TOKEN_INVALID_SIGNATURE = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
  public static final String AUTHORIZATION = "Authorization";

  private final SelfSignedTokenClient tokenClient = new SelfSignedTokenClient();

  interface ApiMethod {
    Response call(RequestSpecification requestSpecification);
  }

  @DataPoints("API Methods")
  public static ApiMethod[] apiMethods =
      new ApiMethod[]{
          spec -> spec.body(eoBody()).post(helper.createUrl())
      };

  @DataPoints("Token Types")
  public static TokenType[] tokenTypes = new TokenType[]{AD, B2C};

  @Theory
  public void callApi_withoutBasicAuth_respondsWith400Error(
      @FromDataPoints("API Methods") ApiMethod apiMethod) {
    RequestSpecification spec =
        given()
            .contentType(ContentType.JSON);
    apiMethod.call(spec)
        .then()
        .statusCode(400);
  }

  @Theory
  public void callApi_withInvalidBasicAuth_respondsWith400Error(
      @FromDataPoints("API Methods") ApiMethod apiMethod) {
    RequestSpecification spec =
        given()
            .contentType(ContentType.JSON)
            .header(X_AUTH_BASIC, helper.getEncodedInvalidBasicAuth());
    apiMethod.call(spec)
        .then()
        .statusCode(400);
  }

  @Theory
  public void callApi_withoutBearerToken_respondsWith401Error(
      @FromDataPoints("API Methods") ApiMethod apiMethod) {
    RequestSpecification spec =
        given()
            .contentType(ContentType.JSON)
            .header(X_AUTH_BASIC, helper.getBasicAuthHeader());
    apiMethod.call(spec)
        .then()
        .statusCode(401);
  }

  @Theory
  public void callApi_withUnrecognisedSignature_respondsWith401Error(
      @FromDataPoints("API Methods") ApiMethod apiMethod) {
    RequestSpecification spec =
        given()
            .contentType(ContentType.JSON)
            .header(X_AUTH_BASIC, helper.getBasicAuthHeader())
            .header(AUTHORIZATION, BEARER + TOKEN_INVALID_SIGNATURE);
    apiMethod.call(spec)
        .then()
        .statusCode(401);
  }

  @Theory
  public void callApi_withExpiredToken_respondsWith401Error(
      @FromDataPoints("API Methods") ApiMethod apiMethod,
      @FromDataPoints("Token Types") TokenType tokenType) {
    RequestSpecification spec =
        given()
            .contentType(ContentType.JSON)
            .header(X_AUTH_BASIC, helper.getBasicAuthHeader())
            .header(AUTHORIZATION, BEARER + tokenClient.getExpiredToken(tokenType));
    apiMethod.call(spec)
        .then()
        .statusCode(401);
  }

  @Theory
  public void callApi_withIncorrectAudience_respondsWith401Error(
      @FromDataPoints("API Methods") ApiMethod apiMethod,
      @FromDataPoints("Token Types") TokenType tokenType) {
    RequestSpecification spec =
        given()
            .contentType(ContentType.JSON)
            .header(X_AUTH_BASIC, helper.getBasicAuthHeader())
            .header(
                AUTHORIZATION,
                BEARER
                    + tokenClient.getTokenWithClaim(tokenType, AUD, "invalid-audience"));
    apiMethod.call(spec).then().statusCode(401);
  }

  @Theory
  public void callApi_withIncorrectIssuer_respondsWith401Error(
      @FromDataPoints("API Methods") ApiMethod apiMethod,
      @FromDataPoints("Token Types") TokenType tokenType) {
    RequestSpecification spec =
        given()
            .contentType(ContentType.JSON)
            .header(X_AUTH_BASIC, helper.getBasicAuthHeader())
            .header(
                AUTHORIZATION,
                BEARER
                    + tokenClient.getTokenWithClaim(tokenType, ISS, "invalid-issuer"));
    apiMethod.call(spec).then().statusCode(401);
  }

  private static String eoBody() {
    return helper.getAsJsonString(Certificate.newInstance());
  }

}
