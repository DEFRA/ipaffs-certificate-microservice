package uk.gov.defra.tracesx.certificate.integration;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CertificateTestConfig.class)
public class CertificateServiceTest {

  private final String REFERENCE = "CHEDA.GB.2018.1234567";
  private final String REFERENCE_XI = "CHEDA.XI.2018.1234567";
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
  public void shouldReturnBadRequestCreateCertificateFromHtmlForXI() {

    String htmlContent = "<p>hello world</p>";
    String callBackUrl = "";
    Response response = certificateApi.getPdf(htmlContent, REFERENCE_XI, callBackUrl);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
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
  public void shouldCreateCertificateWithFont() {
    String htmlContent = "<p style='font-family: Arial Unicode MS;'>hello world</p>";
    String callBackUrl = "";
    Response response = certificateApi.getPdf(htmlContent, REFERENCE, callBackUrl);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SC_OK);
  }
}
