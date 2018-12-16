package uk.gov.defra.tracesx.certificate.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.core.context.SecurityContextHolder.setContext;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationFacadeTest {

  private final SecurityContextImpl securityContext = new SecurityContextImpl();
  private static final String CERTIFICATE_CREATE = "certificate.create";

  @Mock private Authentication authentication;

  @InjectMocks private AuthenticationFacade authenticationFacade;

  @Before
  public void setup() {
    securityContext.setAuthentication(authentication);
    setContext(securityContext);
  }

  @Test
  public void testWhenGetAuthenticationThenReturnsAuthentication() {

    final Authentication facadeAuthentication = authenticationFacade.getAuthentication();
    assertThat(facadeAuthentication).isNotNull();
    assertThat(facadeAuthentication).isEqualTo(authentication);
  }

  @Test
  public void testWhenReplaceAuthoritiesThenGetAuthorityReturnsNewPermissions() {
    final List<SimpleGrantedAuthority> grantedAuthoritiesList = new ArrayList<>();
    final SimpleGrantedAuthority authority = new SimpleGrantedAuthority(CERTIFICATE_CREATE);
    grantedAuthoritiesList.add(authority);

    authenticationFacade.replaceAuthorities(grantedAuthoritiesList);

    assertThat(
            securityContext.getAuthentication().getAuthorities().iterator().next().getAuthority())
        .isEqualTo(CERTIFICATE_CREATE);
  }
}
