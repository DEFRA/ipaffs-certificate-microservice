package uk.gov.defra.tracesx.certificate;

import java.io.FileNotFoundException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.gov.defra.tracesx.certificate.utillities.FontFile;

@Configuration
@EnableConfigurationProperties
public class CertificateConfiguration {

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public FontFile fontFile() throws FileNotFoundException {
    return new FontFile("Arial", "Arial Unicode.ttf");
  }
}
