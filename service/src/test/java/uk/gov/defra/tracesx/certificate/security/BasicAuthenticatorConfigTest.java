package uk.gov.defra.tracesx.certificate.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.test.util.ReflectionTestUtils;
@RunWith(MockitoJUnitRunner.class)
public class BasicAuthenticatorConfigTest {

    private static final String BASE_URL_MATCHER = "/certificate/*";

    private final BasicAuthenticatorConfig testee = new BasicAuthenticatorConfig();

    @Mock
    private ObjectPostProcessor<Object> opp;
    @Mock
    private AuthenticationManager parent;
    @Mock
    private AuthenticationManagerBuilder builder;
    @Mock
    private AuthenticationConfiguration authenticationConfigurationMock;

    @Test
    public void whenBasicAuthConfigIsCalledReturnsValidConfiguration() throws Exception {

        ReflectionTestUtils.setField(testee,  "authenticationConfiguration", authenticationConfigurationMock);

        when(authenticationConfigurationMock.getAuthenticationManager()).thenReturn(parent);

        final Map<Class<? extends Object>, Object> sharedObjects = new HashMap<>();
        builder.parentAuthenticationManager(parent);
        builder.build();
        HttpSecurity httpSecurity = new HttpSecurity(opp, builder, sharedObjects);
        testee.configure(httpSecurity);
        final List<ArrayList> urlMappings = (ArrayList) ReflectionTestUtils.getField(
                httpSecurity.getConfigurer(ExpressionUrlAuthorizationConfigurer.class).getRegistry(),
                "urlMappings");

        assertThat(urlMappings.size()).isEqualTo(3);
        assertThat(ReflectionTestUtils.getField(ReflectionTestUtils.getField(urlMappings.iterator().next(),"requestMatcher"), "pattern")).isEqualTo(BASE_URL_MATCHER);
    }
}

