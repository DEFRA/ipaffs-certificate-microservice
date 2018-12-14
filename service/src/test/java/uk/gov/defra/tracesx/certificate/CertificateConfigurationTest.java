package uk.gov.defra.tracesx.certificate;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.handler.MappedInterceptor;
import uk.gov.defra.tracesx.certificate.security.CertificateAuthFilter;

@RunWith(MockitoJUnitRunner.class)
public class CertificateConfigurationTest {
    
    private static final int CONNECTION_TIMEOUT = 1;
    private static final int READ_TIMEOUT = 2;
    private static final String TEST_USER = "testUser";
    private static final String TEST_PASSWORD = "testPassword";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String PERMISSIONS_SERVICE_CONNECTION_TIMEOUT = "permissionsServiceConnectionTimeout";
    private static final String PERMISSIONS_SERVICE_READ_TIMEOUT = "permissionsServiceReadTimeout";
    private static final String PERMISSIONS_SERVICE_USER = "permissionsServiceUser";
    private static final String PERMISSIONS_SERVICE_PASSWORD = "permissionsServicePassword";
    private static final String BASE_URL_MATCHER = "/certificate/*";

    @Mock
    private CertificateAuthFilter certificateAuthFilter;

    private final CertificateConfiguration testee = new CertificateConfiguration();

    @Before
    public void setUp() {
        ReflectionTestUtils
                .setField(testee, PERMISSIONS_SERVICE_CONNECTION_TIMEOUT, CONNECTION_TIMEOUT);
        ReflectionTestUtils.setField(testee, PERMISSIONS_SERVICE_READ_TIMEOUT, READ_TIMEOUT);
        ReflectionTestUtils.setField(testee, PERMISSIONS_SERVICE_USER, TEST_USER);
        ReflectionTestUtils.setField(testee, PERMISSIONS_SERVICE_PASSWORD, TEST_PASSWORD);
        ReflectionTestUtils.setField(testee,"certificateAuthFilter", certificateAuthFilter);
    }

    @Test
    public void whenAuthFilterRegistrationThenReturnFilter() {
        FilterRegistrationBean filterRegistrationBean = testee
                .commodityCategoryAuthFilterRegistration();

        assertThat(filterRegistrationBean.getOrder()).isEqualTo(1);
        assertThat(filterRegistrationBean.getUrlPatterns().size()).isEqualTo(1);
        assertThat(filterRegistrationBean.getUrlPatterns()).containsExactly(BASE_URL_MATCHER);
    }

    @Test
    public void whenPermissionsRestTemplateCalledReturnsValidConfiguration() {
        final RestTemplate restTemplate = testee.permissionsRestTemplate();
        assertThat(restTemplate).isNotNull();
        assertThat(restTemplate.getInterceptors().size()).isEqualTo(1);
        final BasicAuthorizationInterceptor basicAuthorizationInterceptor = (BasicAuthorizationInterceptor) restTemplate.getInterceptors().get(0);
        //Test basic authorisation interceptor is available
        assertThat(basicAuthorizationInterceptor).isNotNull();
        assertThat(ReflectionTestUtils.getField(basicAuthorizationInterceptor, USERNAME)).isEqualTo(TEST_USER);
        assertThat(ReflectionTestUtils.getField(basicAuthorizationInterceptor, PASSWORD)).isEqualTo(TEST_PASSWORD);
        //Test Spring default message converters registered
        assertThat(restTemplate.getMessageConverters()).isNotEmpty();
    }


    @Test
    public void whenAddInterceptorsThenReturnInterceptors() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException{
        InterceptorRegistry interceptorRegistry = new InterceptorRegistry();
        testee.addInterceptors(interceptorRegistry);
        interceptorRegistry.getClass().getDeclaredMethod("getInterceptors");
        Method retrieveItems = interceptorRegistry.getClass().getDeclaredMethod("getInterceptors");
        retrieveItems.setAccessible(true);
        List<MappedInterceptor> interceptorList = (List<MappedInterceptor>)retrieveItems.invoke(interceptorRegistry);

        assertThat(interceptorList).isNotNull();
        assertThat(interceptorList).isNotEmpty();
        assertThat(interceptorList.get(0).getPathPatterns()).containsExactly(BASE_URL_MATCHER);
    }
}
