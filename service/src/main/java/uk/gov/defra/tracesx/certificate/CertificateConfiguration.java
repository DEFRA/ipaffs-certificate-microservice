package uk.gov.defra.tracesx.certificate;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.defra.tracesx.certificate.utils.FontFile;

@Configuration
@ComponentScan("uk.gov.defra.tracesx.common.health")
public class CertificateConfiguration implements WebMvcConfigurer {

  @Bean
  public FontFile fontFile() {
    return new FontFile("Times New Roman", "Times New Roman.ttf");
  }

  @Bean
  public FontFile fontFileBold() {
    return new FontFile("Times New Roman Bold", "Times New Roman Bold.ttf");
  }

  @Bean
  public RestTemplate httpClient() {
    return new RestTemplate();
  }
}
