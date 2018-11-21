package uk.gov.defra.tracesx.certificate.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.client.RestTemplate;

@Configuration
@PropertySource("local-dev.properties")
public class CertificateTestConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(CertificateTestConfig.class);

  @Value("${username}")
  private String username;
  @Value("${password}")
  private String password;
  @Value("${url}")
  private String url;

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public TestEnvironment testEnvironment() {
    if(hasEnvProperties()) {
      LOGGER.info("env properties found");
      return new TestEnvironment(getUsername(), getPassword(), getUrl());
    }
    LOGGER.info("env properties NOT found, defaulting to local settings");
    return new TestEnvironment(username, password, url);
  }

  private boolean hasEnvProperties() {
    return getUsername() != null && getPassword() != null && getUrl() != null;
  }

  private String getUrl() {
    return System.getProperty("service.base.url");
  }

  private String getPassword() {
    return System.getProperty("auth.password");
  }

  private String getUsername() {
    return System.getProperty("auth.username");
  }

  @Bean
  public CertificateApi certificateApi() {
    return new CertificateApi(testEnvironment());
  }
}
