package uk.gov.defra.tracesx.certificate.integration;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertNotNull;

import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.util.UriComponentsBuilder;

public class TestBasicEndpoints {

  private String baseUrl;
  private String resourceUrl;
  private String userName;
  private String password;

  @Before
  public void setup() {

    userName = System.getProperty("auth.username");
    password = System.getProperty("auth.password");
    baseUrl = System.getProperty("service.base.url");
    checkProperties();

    resourceUrl = UriComponentsBuilder.fromHttpUrl(baseUrl).path("certificate").build().toString();
  }

  private void checkProperties() {
    assertNotNull("User name is empty", userName);
    assertNotNull("Password is empty", password);
    assertNotNull("Url is empty", baseUrl);
  }

  @Test
  public void canDownloadDocument() {

    given()
        .auth()
        .basic(userName, password)
        .when()
        .get(resourceUrl + "/download/" + UUID.randomUUID())
        .then()
        .statusCode(200);
  }
}
