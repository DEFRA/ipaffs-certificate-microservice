package uk.gov.defra.tracesx.certificate.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@RunWith(MockitoJUnitRunner.class)
public class CustomBasicAuthenticationFilterTest {

    private static final String TEST_USER = "testUser";
    private static final String TEST_PASSWORD = "testPassword";
    private static final String ROLE_TEST = "ROLE_TEST";
    private static final String BASIC = "Basic ";
    private static final String BASIC_IN_VALID_BASE_64 = "Basic INVALID_BASE64";
    private static final String VALID_CREDENTIALS = TEST_USER+":"+TEST_PASSWORD;
    private static final String INVALID_CREDENTIALS = "otherUser:WRONG_PASSWORD";;
    private static final String VALID_ENCODED_CREDENTIALS =BASIC + new String(Base64.encodeBase64(VALID_CREDENTIALS.getBytes()));
    private static final String INVALID_TOKEN = "invalidToken";
    private static final String INVALID_TOKEN_AS_MISSING_COLON = "INVALID_TOKEN_AS_MISSING_COLON";
    private static final UsernamePasswordAuthenticationToken testAuthenticationToken = new UsernamePasswordAuthenticationToken(
            TEST_USER, TEST_PASSWORD);
    private static final Authentication testAuthentication = new UsernamePasswordAuthenticationToken(TEST_USER, TEST_PASSWORD,
            AuthorityUtils.createAuthorityList(ROLE_TEST));
    private static final String BASIC_AUTH_HEADER_KEY = "x-auth-basic";

    @Mock
    private HttpServletRequest requestMock;
    @Mock
    private HttpServletResponse responseMock;
    @Mock
    private FilterChain chainMock;
    @Mock
    private AuthenticationManager authenticationManagerMock;
    @InjectMocks
    private CustomBasicAuthenticationFilter testee;

    @Before
    public void setUp() {

        SecurityContextHolder.clearContext();

        when(authenticationManagerMock.authenticate(testAuthenticationToken)).thenReturn(testAuthentication);
        when(authenticationManagerMock.authenticate(not(eq(testAuthenticationToken)))).thenThrow(
                new BadCredentialsException(""));
    }

    @After
    public void clearContext() throws Exception {
        SecurityContextHolder.clearContext();
    }

    @Test(expected = BadCredentialsException.class)
    public void testFilterIgnoresRequestsContainingNoAuthorizationHeader()
            throws Exception {
        testee.doFilter(requestMock, responseMock, chainMock);

        verify(chainMock).doFilter(any(ServletRequest.class), any(ServletResponse.class));

        // Test
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    public void testInvalidBasicAuthorizationTokenIsIgnored() throws Exception {
        when(requestMock.getHeader(BASIC_AUTH_HEADER_KEY)).thenReturn(BASIC + new String(Base64.encodeBase64(INVALID_TOKEN_AS_MISSING_COLON.getBytes())));

        testee.doFilter(requestMock, responseMock, chainMock);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    public void invalidBase64IsIgnored() throws Exception {
        when(requestMock.getHeader(BASIC_AUTH_HEADER_KEY)).thenReturn(BASIC_IN_VALID_BASE_64);

        testee.doFilter(requestMock, responseMock, chainMock);
        // The filter chain shouldn't proceed
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    public void testInvalidTokenPasswordOperation() throws Exception {
        when(requestMock.getHeader(BASIC_AUTH_HEADER_KEY)).thenReturn(BASIC + new String(Base64.encodeBase64(INVALID_TOKEN.getBytes())));

        // Test
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        testee.doFilter(requestMock, new MockHttpServletResponse(), chainMock);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    public void testNormalOperation() throws Exception {

        when(requestMock.getHeader(BASIC_AUTH_HEADER_KEY)).thenReturn(VALID_ENCODED_CREDENTIALS);

        // Test
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        testee.doFilter(requestMock, new MockHttpServletResponse(), chainMock);

        verify(chainMock).doFilter(any(ServletRequest.class), any(ServletResponse.class));
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .isEqualTo(TEST_USER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartupDetectsMissingAuthenticationManager() throws Exception {
        final BasicAuthenticationFilter filter = new BasicAuthenticationFilter(null);
    }

    @Test
    public void testSuccessLoginThenFailureLoginResultsInSessionLosingToken()
            throws Exception {
        when(requestMock.getHeader(BASIC_AUTH_HEADER_KEY)).thenReturn(VALID_ENCODED_CREDENTIALS);
        final MockHttpServletResponse response1 = new MockHttpServletResponse();

        testee.doFilter(requestMock, response1, chainMock);

        verify(chainMock).doFilter(any(ServletRequest.class), any(ServletResponse.class));

        // Test
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(TEST_USER);

        // NOW PERFORM FAILED AUTHENTICATION
        when(requestMock.getHeader(BASIC_AUTH_HEADER_KEY)).thenReturn(BASIC + new String(Base64.encodeBase64(INVALID_CREDENTIALS.getBytes())));
        final MockHttpServletResponse response2 = new MockHttpServletResponse();

        testee.doFilter(requestMock, response2, chainMock);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
