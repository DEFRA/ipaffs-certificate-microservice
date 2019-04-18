package uk.gov.defra.tracesx.certificate.security.jwt;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.defra.tracesx.certificate.exceptions.UnauthorizedException;
import uk.gov.defra.tracesx.certificate.security.IdTokenAuthentication;
import uk.gov.defra.tracesx.certificate.security.IdTokenUserDetails;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

  private JwtTokenValidator jwtTokenValidator;

  public JwtTokenFilter(JwtTokenValidator jwtTokenValidator) {
    this.jwtTokenValidator = jwtTokenValidator;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest req, HttpServletResponse res, FilterChain filterChain)
      throws IOException, ServletException {
    try {
      String token = resolveToken(req);
      if (null != token) {
        IdTokenUserDetails userDetails = jwtTokenValidator.validateToken(token);
        IdTokenAuthentication authentication = new IdTokenAuthentication(userDetails);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
      filterChain.doFilter(req, res);
    } catch (UnauthorizedException exception) {
      res.sendError(HttpServletResponse.SC_UNAUTHORIZED, exception.getMessage());
    }
  }

  private String resolveToken(HttpServletRequest req) {
    String bearerToken = req.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
