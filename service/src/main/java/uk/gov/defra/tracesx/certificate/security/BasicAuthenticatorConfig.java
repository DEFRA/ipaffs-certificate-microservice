package uk.gov.defra.tracesx.certificate.security;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class BasicAuthenticatorConfig extends WebSecurityConfigurerAdapter {

  private static final String BASE_URL_MATCHER = "/certificate/*";
  private static final String ADMIN_URL_MATCHER = "/admin/*";

  @Override
  protected void configure(HttpSecurity http) throws Exception {

    http.authorizeRequests()
            .antMatchers(BASE_URL_MATCHER, ADMIN_URL_MATCHER)
            .fullyAuthenticated()
            .anyRequest().denyAll()
            .and()
            .addFilterBefore(new CustomBasicAuthenticationFilter(authenticationManager()), UsernamePasswordAuthenticationFilter.class)
            .csrf()
            .disable()
            .exceptionHandling().authenticationEntryPoint(unauthorizedEntryPoint());
    http.sessionManagement().sessionCreationPolicy(STATELESS);
  }

  @Bean
  public AuthenticationEntryPoint unauthorizedEntryPoint() {
    return (request, response, authException) -> response.sendError(SC_UNAUTHORIZED);
  }
}
