package uk.gov.defra.tracesx.certificate.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.defra.tracesx.integration.certificate.Properties.FRONTEND_NOTIFICATION_URL;

import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.io.IOException;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CertificateTestConfig.class)
public class CertificateServiceTest {

  private final String REFERENCE = "CHEDA.GB.2018.1234567";
  private final String REFERENCE_INVALID = "CHEDA.INVALID.2018.1234567";

  @Autowired
  private CertificateApi certificateApi;

  @Test
  public void shouldCreateCertificateFromHtml() {

    String htmlContent = "<p>hello world</p>";
    String callBackUrl = "";
    Response response = certificateApi.getPdf(htmlContent, REFERENCE, callBackUrl);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_OK);
  }

  @Test
  public void shouldReturnBadRequestIfCountryCodeIsInvalid() {

    String htmlContent = "<p>hello world</p>";
    String callBackUrl = "";
    Response response = certificateApi.getPdf(htmlContent, REFERENCE_INVALID, callBackUrl);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  public void shouldReturnBadRequestIfHtmlIsInvalid() {
    String htmlContent = "<p hello world";
    String callBackUrl = "";
    Response response = certificateApi.getPdf(htmlContent, REFERENCE, callBackUrl);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  public void shouldCreateCertificateWithFontAndStyles() throws IOException {
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("certificate.html");
    String htmlContent = IOUtils.toString(inputStream, Charset.defaultCharset());
    String callBackUrl = FRONTEND_NOTIFICATION_URL;
    Response response = certificateApi.getPdf(htmlContent, REFERENCE, callBackUrl);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_OK);
  }
}
