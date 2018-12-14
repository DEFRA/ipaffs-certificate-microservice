package uk.gov.defra.tracesx.certificate.integration;

import static io.restassured.RestAssured.given;
import static java.nio.charset.StandardCharsets.UTF_8;

import io.restassured.http.ContentType;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import org.junit.Before;
import org.junit.Test;

public class TestAdminAuthentication {

    public static final String X_AUTH_BASIC = "x-auth-basic";
    public static final String BASIC = "Basic ";
    public static final String COLON = ":";
    public static final String INVALID_BASIC_AUTH = "Base invalidBasicAuth";
    private String baseUrl;
    private String userName;
    private String password;
    private String encodedBasicAuth;


    @Before
    public void setup() throws UnsupportedEncodingException {
        userName = System.getProperty("auth.username");
        password = System.getProperty("auth.password");

        baseUrl = System.getProperty("service.base.url");
        encodedBasicAuth = BASIC + Base64.getEncoder().encodeToString(
                new StringBuffer().append(userName).append(COLON).append(password).toString().getBytes(
                        UTF_8.name()));
    }

    @Test
    public void callAdmin_withoutBasicAuth_respondsWith400Error() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get(baseUrl+ "/admin/info")
                .then()
                .statusCode(401);
    }

    @Test
    public void callAdmin_withInvalidBasicAuth_respondsWith400Error() {
        given()
                .contentType(ContentType.JSON)
                .header(X_AUTH_BASIC, INVALID_BASIC_AUTH)
                .when()
                .get(baseUrl+ "/admin/info")
                .then()
                .statusCode(401);
    }

    @Test
    public void callAdmin_withValidBasicAuth_successfully() {
        given()
                .contentType(ContentType.JSON)
                .header(X_AUTH_BASIC,encodedBasicAuth)
                .when()
                .get(baseUrl+ "/admin/info")
                .then()
                .statusCode(200);
    }

}