package uk.gov.defra.tracesx.certificate.integration;

import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

class CertificateServiceIT extends IntegrationBase {

  private final String REFERENCE = "CHEDA.GB.2018.1234567";
  private final String REFERENCE_INVALID = "CHEDA.INVALID.2018.1234567";

  @Value("${frontendNotification.service.url}")
  private String frontendNotificationUrl;

  @BeforeEach
  void setup() {
    givenUserHasPermissions(
        "certificate.create"
    );
  }

  @Test
  void shouldCreateCertificateFromHtml() {
    String htmlContent = "<p>hello world</p>";
    String callBackUrl = "";

    var response = getPdf(htmlContent, REFERENCE, callBackUrl);

    assertThat(response.getStatus().value()).isEqualTo(SC_OK);
  }

  @Test
  void shouldReturnBadRequestIfCountryCodeIsInvalid() {
    String htmlContent = "<p>hello world</p>";
    String callBackUrl = "";

    var response = getPdf(htmlContent, REFERENCE_INVALID, callBackUrl);

    assertThat(response.getStatus().value()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  void shouldReturnBadRequestIfHtmlIsInvalid() {
    String htmlContent = "<p hello world";
    String callBackUrl = "";

    var response = getPdf(htmlContent, REFERENCE, callBackUrl);

    assertThat(response.getStatus().value()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  void shouldCreateCertificateWithFontAndStyles() throws IOException {

    usingStub()
        .when(
            request()
                .withMethod("GET")
                .withPath("/public/stylesheets/certificate.css"))
        .respond(response()
            .withHeader("Content-Type", "text/css")
            .withStatusCode(200)
            .withBody("body { font-family: Arial, sans-serif; } /* certificate styles here */"));

    usingStub()
        .when(
            request()
                .withMethod("GET")
                .withPath("/public/images/statuses/draft.png"))
        .respond(response()
            .withHeader("Content-Type", "image/png")
            .withStatusCode(200)
            .withBody("draft.png"));

    Resource resource = resourceLoader.getResource("classpath:integration/certificate.html");
    byte[] bytes = IOUtils.toByteArray(resource.getInputStream());
    String htmlContent = new String(bytes, Charset.defaultCharset());
    String callBackUrl = frontendNotificationUrl;

    var response = getPdf(htmlContent, REFERENCE, callBackUrl);

    assertThat(response.getStatus().value()).isEqualTo(SC_OK);
  }

  private EntityExchangeResult<String> getPdf(String htmlContent, String reference, String url) {

    return webClient("AnaAdams")
        .post()
        .uri("/certificate/" + reference + "?url=" + url)
        .bodyValue(htmlContent)
        .exchange()
        .expectBody(String.class)
        .returnResult();
  }

}
