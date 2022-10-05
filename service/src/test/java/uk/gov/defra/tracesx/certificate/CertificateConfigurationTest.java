package uk.gov.defra.tracesx.certificate;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

class CertificateConfigurationTest {

  private final CertificateConfiguration certificateConfiguration = new CertificateConfiguration();

  @Test
  void httpClient_ReturnsRestTemplate() {
    assertThat(certificateConfiguration.httpClient()).isInstanceOf(RestTemplate.class);
  }

  @Test
  void fontFile_ReturnsTimesNewRoman() {
    assertThat(certificateConfiguration.fontFile().getName()).isEqualTo("Times New Roman");
  }

  @Test
  void fontFileBold_ReturnsTimesNewRomanBold() {
    assertThat(certificateConfiguration.fontFileBold().getName()).isEqualTo("Times New Roman Bold");
  }

}
