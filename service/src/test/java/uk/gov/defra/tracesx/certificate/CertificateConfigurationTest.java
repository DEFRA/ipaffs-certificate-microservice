package uk.gov.defra.tracesx.certificate;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.handler.MappedInterceptor;
import uk.gov.defra.tracesx.certificate.security.CertificateAuthFilter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class CertificateConfigurationTest {

  private static final String BASE_URL_MATCHER = "/certificate/*";

  @Mock
  private CertificateAuthFilter certificateAuthFilter;

  private final CertificateConfiguration testee = new CertificateConfiguration();

  @Before
  public void setUp() {
    ReflectionTestUtils.setField(testee, "certificateAuthFilter", certificateAuthFilter);
  }

  @Test
  public void whenAuthFilterRegistrationThenReturnFilter() {
    FilterRegistrationBean filterRegistrationBean = testee.certificateAuthFilterRegistration();

    assertThat(filterRegistrationBean.getOrder()).isEqualTo(2);
    assertThat(filterRegistrationBean.getUrlPatterns().size()).isEqualTo(1);
    assertThat(filterRegistrationBean.getUrlPatterns()).containsExactly(BASE_URL_MATCHER);
  }

  @Test
  public void whenAddInterceptorsThenReturnInterceptors()
      throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
    InterceptorRegistry interceptorRegistry = new InterceptorRegistry();
    testee.addInterceptors(interceptorRegistry);
    interceptorRegistry.getClass().getDeclaredMethod("getInterceptors");
    Method retrieveItems = interceptorRegistry.getClass().getDeclaredMethod("getInterceptors");
    retrieveItems.setAccessible(true);
    List<MappedInterceptor> interceptorList =
        (List<MappedInterceptor>) retrieveItems.invoke(interceptorRegistry);

    assertThat(interceptorList).isNotNull();
    assertThat(interceptorList).isNotEmpty();
    assertThat(interceptorList.get(0).getPathPatterns()).containsExactly(BASE_URL_MATCHER);
  }
}
