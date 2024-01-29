package uk.gov.defra.tracesx.certificate.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.defra.tracesx.integration.certificate.Properties.FRONTEND_NOTIFICATION_URL;

import io.restassured.response.Response;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CertificateTestConfig.class)
class CertificateServiceTest {

  private final String REFERENCE = "CHEDA.GB.2018.1234567";
  private final String REFERENCE_INVALID = "CHEDA.INVALID.2018.1234567";

  @Autowired
  private CertificateApi certificateApi;

  @Test
  void shouldCreateCertificateFromHtml() {

    String htmlContent = "<p>hello world</p>";
    String callBackUrl = "";
    Response response = certificateApi.getPdf(htmlContent, REFERENCE, callBackUrl);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_OK);
  }

  @Test
  void shouldReturnBadRequestIfCountryCodeIsInvalid() {

    String htmlContent = "<p>hello world</p>";
    String callBackUrl = "";
    Response response = certificateApi.getPdf(htmlContent, REFERENCE_INVALID, callBackUrl);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  void shouldReturnBadRequestIfHtmlIsInvalid() {
    String htmlContent = "<p hello world";
    String callBackUrl = "";
    Response response = certificateApi.getPdf(htmlContent, REFERENCE, callBackUrl);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  void shouldCreateCertificateWithFontAndStyles() throws IOException {
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("certificate.html");
    String htmlContent = IOUtils.toString(inputStream, Charset.defaultCharset());
    String callBackUrl = FRONTEND_NOTIFICATION_URL;
    Response response = certificateApi.getPdf(htmlContent, REFERENCE, callBackUrl);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_OK);
  }
}
