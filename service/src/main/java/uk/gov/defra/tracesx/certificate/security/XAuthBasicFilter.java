package uk.gov.defra.tracesx.certificate.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class XAuthBasicFilter extends OncePerRequestFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(XAuthBasicFilter.class);

  static final String BASIC_AUTH_HEADER_KEY = "x-auth-basic";

  private final AuthenticationManager authenticationManager;

  public XAuthBasicFilter(AuthenticationManager authenticationManager) {
    this.authenticationManager = authenticationManager;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    String header = request.getHeader(BASIC_AUTH_HEADER_KEY);
    try {
      validateHeader(header);
      String[] credentials = extractAndDecodeHeader(header);
      validateCredentials(credentials);
    } catch (BadCredentialsException exception) {
      LOGGER.error("Authentication failed", exception);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
      return;
    }
    chain.doFilter(request, response);
  }

  private void validateHeader(String header) {
    if (header == null) {
      throw new BadCredentialsException("Request missing " + BASIC_AUTH_HEADER_KEY + " header.");
    } else if (!header.toLowerCase().startsWith("basic ")) {
      throw new BadCredentialsException(
          "The header " + BASIC_AUTH_HEADER_KEY + " contains an invalid format.");
    }
  }

  private String[] extractAndDecodeHeader(String header) throws BadCredentialsException {
    try {
      byte[] base64Token = header.substring("Basic ".length()).getBytes(StandardCharsets.UTF_8);
      byte[] decoded = Base64.getDecoder().decode(base64Token);
      String token = new String(decoded, StandardCharsets.UTF_8);
      int delim = token.indexOf(':');
      if (delim == -1) {
        throw new BadCredentialsException("Invalid basic authentication token");
      }
      return new String[] {token.substring(0, delim), token.substring(delim + 1)};
    } catch (IllegalArgumentException exception) {
      throw new BadCredentialsException("Failed to decode basic authentication token");
    }
  }

  private void validateCredentials(String[] credentials) {
    UsernamePasswordAuthenticationToken basicToken =
        new UsernamePasswordAuthenticationToken(credentials[0], credentials[1]);
    authenticationManager.authenticate(basicToken);
  }
}
