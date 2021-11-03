package uk.gov.defra.tracesx.certificate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class CertificateConfigurationTest {

  private final CertificateConfiguration certificateConfiguration = new CertificateConfiguration();

  @Test
  public void httpClientReturnsRestTemplate(){
    assertThat(certificateConfiguration.httpClient()).isInstanceOf(RestTemplate.class);
  }

}
