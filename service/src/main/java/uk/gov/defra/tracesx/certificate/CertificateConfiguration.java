package uk.gov.defra.tracesx.certificate;

import static java.util.Arrays.asList;

import java.io.FileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.defra.tracesx.certificate.security.CertificateAuthFilter;
import uk.gov.defra.tracesx.certificate.security.PreAuthorizeChecker;
import uk.gov.defra.tracesx.certificate.utillities.FontFile;

@Configuration
@EnableConfigurationProperties
public class CertificateConfiguration implements WebMvcConfigurer {

    private static final String BASE_URL_MATCHER = "/certificate/*";
    public static final String CERTIFICATE_AUTH_FILTER = "certificateAuthFilter";
    public static final int CERTFICATE_ORDER = 1;
    @Value("${permissions.service.connectionTimeout}")
    private int permissionsServiceConnectionTimeout;

    @Value("${permissions.service.readTimeout}")
    private int permissionsServiceReadTimeout;

    @Value("${permissions.service.user}")
    private String permissionsServiceUser;

    @Value("${permissions.service.password}")
    private String permissionsServicePassword;

    @Autowired
    private CertificateAuthFilter certificateAuthFilter;

    @Bean
    public FontFile fontFile() throws FileNotFoundException {
        return new FontFile("Arial", "Arial Unicode.ttf");
    }

    @Bean(name = "permissionsRestTemplate")
    public RestTemplate permissionsRestTemplate() {
        return createRestTemplate(
                permissionsServiceConnectionTimeout,
                permissionsServiceReadTimeout,
                permissionsServiceUser,
                permissionsServicePassword);
    }

    private RestTemplate createRestTemplate(
            final int connectionTimeout,
            final int readTimeout,
            final String serviceUser,
            final String servicePassword) {

        final HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(connectionTimeout);
        clientHttpRequestFactory.setReadTimeout(readTimeout);

        final RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
        restTemplate.getInterceptors().add(new BasicAuthorizationInterceptor(serviceUser, servicePassword));
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

        return restTemplate;
    }

    @Bean
    public FilterRegistrationBean commodityCategoryAuthFilterRegistration() {

        FilterRegistrationBean result = new FilterRegistrationBean();
        result.setFilter(certificateAuthFilter);
        result.setUrlPatterns(asList(BASE_URL_MATCHER));
        result.setName(CERTIFICATE_AUTH_FILTER);
        result.setOrder(CERTFICATE_ORDER);
        return result;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new PreAuthorizeChecker())
                .addPathPatterns(new String[] {BASE_URL_MATCHER});
    }
}
