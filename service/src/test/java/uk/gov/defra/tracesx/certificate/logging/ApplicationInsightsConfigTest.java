package uk.gov.defra.tracesx.certificate.logging;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationInsightsConfigTest {

  private static final String APPLICATIONINSIGHTS_CONNECTION_STRING = "APPLICATIONINSIGHTS_CONNECTION_STRING";
  private static final String APPLICATIONINSIGHTS_CONNECTION_STRING_VALUE = "InstrumentationKey=00000000-0000-0000-0000-000000000000";
  private static final String APPLICATION_NAME = "certificate-microservice";
  private static final String BLANK = "";
  @Mock
  private Environment environment;
  @InjectMocks
  private ApplicationInsightsConfig underTest;

  @Test
  public void whenEnvHasVariableSetThenTheResultContainsValue() {

    when(environment.getProperty(APPLICATIONINSIGHTS_CONNECTION_STRING)).thenReturn(
            APPLICATIONINSIGHTS_CONNECTION_STRING_VALUE);
    String result = underTest.telemetryConfig();
    assertThat(result, is(APPLICATIONINSIGHTS_CONNECTION_STRING_VALUE));
  }

  @Test
  public void whenEnvHasVariableSetToBlankThenTheResultDoesntContainValue() {

    when(environment.getProperty(APPLICATIONINSIGHTS_CONNECTION_STRING)).thenReturn(BLANK);
    String result = underTest.telemetryConfig();
    assertThat(result, is(BLANK));
  }

  @Test
  public void whenEnvHasVariableNotSetThenTheResultDoesntContainValue() {

    when(environment.getProperty(APPLICATIONINSIGHTS_CONNECTION_STRING)).thenReturn(null);
    String result = underTest.telemetryConfig();
    assertThat(result, is(nullValue()));
  }

  @Test
  public void filterRegistrationBeanHasCatchAllUrl() {
    //Given
    ApplicationInsightsConfig aiConfig = new ApplicationInsightsConfig();

    //When
    FilterRegistrationBean filterRegistration = aiConfig.aiFilterRegistration(APPLICATION_NAME);

    //Then
    assertEquals(1, filterRegistration.getUrlPatterns().size());
    assertEquals("/*", filterRegistration.getUrlPatterns().iterator().next());
  }

  @Test
  public void filterRegistrationBeanHasHighOrder() {
    //Given
    ApplicationInsightsConfig aiConfig = new ApplicationInsightsConfig();

    //When
    FilterRegistrationBean filterRegistration = aiConfig.aiFilterRegistration(APPLICATION_NAME);

    //Then
    assertEquals(Ordered.HIGHEST_PRECEDENCE + 10, filterRegistration.getOrder());
  }
}
