package uk.gov.defra.tracesx.certificate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryConfiguration;
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
    return new FontFile("Arial", "Arial Unicode.ttf");
  }

  @Bean
  public RestTemplate httpClient() {
    return new RestTemplate();
  }
}
