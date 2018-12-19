package uk.gov.defra.tracesx.certificate.integration;

import static io.restassured.RestAssured.given;
import static uk.gov.defra.tracesx.integration.certificate.helpers.CertificateServiceHelper.X_AUTH_BASIC;

import io.restassured.http.ContentType;
import org.junit.Test;
import uk.gov.defra.tracesx.integration.certificate.helpers.CertificateServiceHelper;

public class TestAdminAuthentication {

  private static final CertificateServiceHelper helper = new CertificateServiceHelper();

  @Test
  public void callAdmin_withoutBasicAuth_respondsWith400Error() {
    given()
        .contentType(ContentType.JSON)
        .when()
        .get(helper.getUrl("/admin"))
        .then()
        .statusCode(400);
  }

  @Test
  public void callAdmin_withInvalidBasicAuth_respondsWith400Error() {
    given()
        .contentType(ContentType.JSON)
        .header(X_AUTH_BASIC, helper.getEncodedInvalidBasicAuth())
        .when()
        .get(helper.getUrl("/admin"))
        .then()
        .statusCode(400);
  }

  @Test
  public void callAdmin_withValidBasicAuth_successfully() {
    given()
        .contentType(ContentType.JSON)
        .header(X_AUTH_BASIC, helper.getBasicAuthHeader())
        .when()
        .get(helper.getUrl("/admin"))
        .then()
        .statusCode(200);
  }
}
