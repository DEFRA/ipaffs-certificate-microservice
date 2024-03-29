package uk.gov.defra.tracesx.certificate.logging;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import com.microsoft.applicationinsights.TelemetryConfiguration;
import com.microsoft.applicationinsights.web.internal.WebRequestTrackingFilter;
import javax.servlet.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

@Configuration
public class ApplicationInsightsConfig {

  private static final String APPLICATIONINSIGHTS_CONNECTION_STRING =
          "APPLICATIONINSIGHTS_CONNECTION_STRING";

  @Autowired
  Environment environment;

  @Bean
  public String telemetryConfig() {

    String connectionString = environment.getProperty(APPLICATIONINSIGHTS_CONNECTION_STRING);
    if (!isEmpty(connectionString)) {
      TelemetryConfiguration.getActive().setConnectionString(connectionString);
    }
    return connectionString;
  }

  // Set AI Web Request Tracking Filter
  @Bean
  public FilterRegistrationBean aiFilterRegistration(
      @Value("${spring.application.name:application}") String applicationName) {
    FilterRegistrationBean registration = new FilterRegistrationBean();
    registration.setFilter(new WebRequestTrackingFilter(applicationName));
    registration.setName("webRequestTrackingFilter");
    registration.addUrlPatterns("/*");
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
    return registration;
  }

  // Set up AI Web Request Tracking Filter
  @Bean(name = "WebRequestTrackingFilter")
  public Filter webRequestTrackingFilter(
      @Value("${spring.application.name:application}") String applicationName) {
    return new WebRequestTrackingFilter(applicationName);
  }
}
