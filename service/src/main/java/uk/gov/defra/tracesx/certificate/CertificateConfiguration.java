package uk.gov.defra.tracesx.certificate;

import static java.util.Arrays.asList;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.defra.tracesx.certificate.security.CertificateAuthFilter;
import uk.gov.defra.tracesx.certificate.security.PreAuthorizeChecker;
import uk.gov.defra.tracesx.certificate.security.jwks.JwksConfiguration;
import uk.gov.defra.tracesx.certificate.utilities.FontFile;

@Configuration
@EnableConfigurationProperties
public class CertificateConfiguration implements WebMvcConfigurer {

  private static final String BASE_URL_MATCHER = "/certificate/*";
  public static final String CERTIFICATE_AUTH_FILTER = "certificateAuthFilter";
  public static final int CERTIFICATE_FILTER_ORDER = 2;

  @Value("${spring.security.jwt.jwks}")
  private String jwkUrl;

  @Value("${spring.security.jwt.iss}")
  private String iss;

  @Value("${spring.security.jwt.aud}")
  private String aud;

  @Autowired private CertificateAuthFilter certificateAuthFilter;

  @Bean
  public FontFile fontFile() {
    return new FontFile("Arial", "Arial Unicode.ttf");
  }

  @Bean
  public RestTemplate httpClient() {
    return new RestTemplate();
  }

  @Bean
  public ObjectMapper getObjectMapper() {
    return new ObjectMapper();
  }

  @Bean
  @Qualifier("jwksConfiguration")
  public List<JwksConfiguration> jwksConfiguration() throws MalformedURLException {
    String[] jwkUrls = jwkUrl.split(",");
    String[] issuers = iss.split(",");
    String[] audiences = aud.split(",");
    List<JwksConfiguration> jwksConfigurations = new ArrayList<>();
    if (jwkUrls.length == issuers.length && issuers.length == audiences.length) {
      for (int i = 0; i < jwkUrls.length; i++) {
        jwksConfigurations.add(
            JwksConfiguration.builder()
                .jwksUrl(new URL(jwkUrls[i]))
                .issuer(issuers[i])
                .audience(audiences[i])
                .build());
      }
      return Collections.unmodifiableList(jwksConfigurations);
    } else {
      throw new IllegalArgumentException(
          "The comma-separated properties spring.security.jwt.[jwks, iss, aud] must all have the same number of elements.");
    }
  }

  @Bean
  public FilterRegistrationBean certificateAuthFilterRegistration() {
    FilterRegistrationBean result = new FilterRegistrationBean();
    result.setFilter(certificateAuthFilter);
    result.setUrlPatterns(asList(BASE_URL_MATCHER));
    result.setName(CERTIFICATE_AUTH_FILTER);
    result.setOrder(CERTIFICATE_FILTER_ORDER);
    return result;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry
        .addInterceptor(new PreAuthorizeChecker())
        .addPathPatterns(BASE_URL_MATCHER);
  }
}
