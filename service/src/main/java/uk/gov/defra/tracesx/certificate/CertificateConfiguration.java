package uk.gov.defra.tracesx.certificate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableConfigurationProperties
@EnableJpaRepositories("uk.gov.defra.tracesx.certificate.dao.repositories")
public class CertificateConfiguration {
  //Custom Configuration properties can be loaded here

  @Bean
  public ObjectMapper getObjectMapper() {
    return new ObjectMapper();
  }
}
