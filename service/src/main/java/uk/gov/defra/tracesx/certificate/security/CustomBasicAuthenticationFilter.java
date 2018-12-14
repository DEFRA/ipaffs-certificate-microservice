package uk.gov.defra.tracesx.certificate.security;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.security.core.context.SecurityContextHolder.clearContext;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;

import java.io.IOException;
import java.util.Base64;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * {@inheritDoc}
 */
public final class CustomBasicAuthenticationFilter extends BasicAuthenticationFilter {

    private static final String BASIC_AUTH_HEADER_KEY = "x-auth-basic";
    private static final String BASIC_AUTH_START = "basic ";
    private static final String FAILED_TO_DECODE_BASIC_AUTH_TOKEN = "Failed to decode basic authentication token";
    private static final String INVALID_BASIC_AUTH_TOKEN = "Invalid basic authentication token";
    private static final String COLON = ":";
    private static final int USER_NAME = 0;
    private static final int PASSWORD = 1;

    CustomBasicAuthenticationFilter(final AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    public void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain)
            throws IOException, ServletException {

        if (request.getHeader(BASIC_AUTH_HEADER_KEY) == null) {
            getContext().setAuthentication(null);
        }
        final String header = request.getHeader(BASIC_AUTH_HEADER_KEY);
        if (header == null || !header.toLowerCase().startsWith(BASIC_AUTH_START)) {
            throw new BadCredentialsException("Bad credentials");
        }
        try {
            final String[] tokens = extractAndDecodeHeader(header, request);
            final String username = tokens[USER_NAME];
            if (authenticationIsRequired(username)) {
                UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, tokens[PASSWORD]);
                Authentication authResult = super.getAuthenticationManager().authenticate(authRequest);
                getContext().setAuthentication(authResult);
            }
        } catch (BadCredentialsException e) {
            clearContext();
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        } catch (AuthenticationException failed) {
            clearContext();
            chain.doFilter(request, response);
            return;
        }
        chain.doFilter(request, response);
    }

    private String[] extractAndDecodeHeader(final String header, final HttpServletRequest request)
            throws IOException {

        final byte[] base64Token = header.substring(BASIC_AUTH_START.length()).getBytes(UTF_8.name());
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(base64Token);
        } catch (IllegalArgumentException e) {
            throw new BadCredentialsException(FAILED_TO_DECODE_BASIC_AUTH_TOKEN);
        }
        final String token = new String(decoded, getCredentialsCharset(request));
        final int delim = token.indexOf(COLON);
        if (delim == -PASSWORD) {
            throw new BadCredentialsException(INVALID_BASIC_AUTH_TOKEN);
        }
        return new String[]{token.substring(USER_NAME, delim), token.substring(delim + PASSWORD)};
    }

    private boolean authenticationIsRequired(final String username) {

        final Authentication existingAuthentication = getContext().getAuthentication();

        if (existingAuthentication == null || !existingAuthentication.isAuthenticated()) {
            return true;
        }
        if (existingAuthentication instanceof UsernamePasswordAuthenticationToken
                && !existingAuthentication.getName().equals(username)) {
            return true;
        }
        return existingAuthentication instanceof AnonymousAuthenticationToken;
    }
}
