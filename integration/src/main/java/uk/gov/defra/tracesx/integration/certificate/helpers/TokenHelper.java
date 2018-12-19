package uk.gov.defra.tracesx.integration.certificate.helpers;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.defra.tracesx.integration.certificate.helpers.JwtConstants.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import uk.gov.defra.tracesx.integration.certificate.Properties;

public class TokenHelper {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  private static final String TEST_OPENID_BASIC;

  static {
    String encodedBasicAuth =
        Base64.getEncoder()
            .encodeToString(
                (Properties.TEST_OPENID_TOKEN_SERVICE_AUTH_USERNAME + ":"
                    + Properties.TEST_OPENID_TOKEN_SERVICE_AUTH_PASSWORD).getBytes(StandardCharsets.UTF_8));
    TEST_OPENID_BASIC = "Basic " + encodedBasicAuth;
  }

  // intentionally cached, do not access directly
  private static Map<String, Object> _claims = null;

  private static synchronized Map<String, Object> getClaims() {
    if (null == _claims) {
      Response response = given()
          .header(AUTHORIZATION, TEST_OPENID_BASIC)
          .when()
          .get(Properties.TEST_OPENID_TOKEN_SERVICE_URL + "/claims");
      response
          .then()
          .statusCode(200);
      _claims = Collections.unmodifiableMap(response.getBody().as(Map.class));
    }
    return _claims;
  }

  private static String createValidTokenBody(Map<String, Object> additionalClaims) {
    long exp = LocalDateTime.now().plusDays(1).toInstant(ZoneOffset.UTC).toEpochMilli() / 1000L;
    Map<String, Object> body = new HashMap<>(getClaims());
    body.put(EXP, exp);
    body.put(NAME, "Test User");
    body.put(UPN, "test.user@test-openid.com");
    body.put(OID, "e48bb725-5fb2-4748-a858-9fabcc454092");
    body.putAll(additionalClaims);
    try {
      return objectMapper.writeValueAsString(body);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private static String createExpiredTokenBody() {
    long exp = LocalDateTime.now().minusDays(1).toInstant(ZoneOffset.UTC).toEpochMilli() / 1000L;
    Map<String, Object> body = new HashMap<>(getClaims());
    body.put(ROLES, Collections.emptyList());
    body.put(EXP, exp);
    body.put(NAME, "Test User");
    body.put(UPN, "test.user@test-openid.com");
    body.put(OID, "e48bb725-5fb2-4748-a858-9fabcc454092");
    try {
      return objectMapper.writeValueAsString(body);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public static String getValidToken(String... roles) {
    Map<String, Object> additionalClaims = new HashMap<>();
    additionalClaims.put(ROLES, Arrays.asList(roles));
    Response response = given()
        .header(AUTHORIZATION, TEST_OPENID_BASIC)
        .when()
        .body(createValidTokenBody(additionalClaims))
        .post(Properties.TEST_OPENID_TOKEN_SERVICE_URL + "/sign");
    response.then()
        .statusCode(200);
    return response.getBody().asString();
  }

  public static String getTokenWithClaims(Map<String, Object> additionalClaims) {
    Response response = given()
        .header(AUTHORIZATION, TEST_OPENID_BASIC)
        .when()
        .body(createValidTokenBody(additionalClaims))
        .post(Properties.TEST_OPENID_TOKEN_SERVICE_URL + "/sign");
    response.then()
        .statusCode(200);
    return response.getBody().asString();
  }

  public static String getExpiredToken() {
    Response response = given()
        .header(AUTHORIZATION, TEST_OPENID_BASIC)
        .when()
        .body(createExpiredTokenBody())
        .post(Properties.TEST_OPENID_TOKEN_SERVICE_URL + "/sign");
    response.then()
        .statusCode(200);
    return response.getBody().asString();
  }

}
