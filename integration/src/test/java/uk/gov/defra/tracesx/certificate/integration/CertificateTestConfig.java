package uk.gov.defra.tracesx.certificate.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class CertificateTestConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(CertificateTestConfig.class);

  @Value("${service.base.url}")
  private String url;

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public TestEnvironment testEnvironment() {
    if(hasEnvProperties()) {
      LOGGER.info("env properties found");
      return new TestEnvironment(getUrl());
    }
    LOGGER.info("env properties NOT found, defaulting to local settings");
    return new TestEnvironment(url);
  }

  private boolean hasEnvProperties() {
    return getUrl() != null;
  }

  private String getUrl() {
    return System.getProperty("service.base.url");
  }


  @Bean
  public CertificateApi certificateApi() {
    return new CertificateApi(testEnvironment());
  }
}
