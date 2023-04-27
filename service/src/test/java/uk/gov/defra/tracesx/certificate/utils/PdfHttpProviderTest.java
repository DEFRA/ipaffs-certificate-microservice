package uk.gov.defra.tracesx.certificate.utils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.openhtmltopdf.extend.FSStream;
import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import uk.gov.defra.tracesx.certificate.exceptions.InvalidTokenException;
import uk.gov.defra.tracesx.certificate.exceptions.UnsupportedHostException;

public class PdfHttpProviderTest {

  private static final String CSS_STYLES_URL = "http://localhost:8000/certificate.css";
  private static final String CSS = "body{font-size:12px;}";
  private static final String JWT_TOKEN = "abc.123.abc";

  @Mock
  private RestTemplate restTemplate;

  @Mock
  private Authentication authentication;

  @Mock
  private SecurityContext securityContext;

  private PdfHttpProvider httpProvider;

  @Before
  public void setUp() {
    initMocks(this);
    httpProvider = new PdfHttpProvider(restTemplate);
    ReflectionTestUtils.setField(httpProvider, "host", "localhost");
    when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  public void givenAnInvalidHost_whenGetUrlIsCalled_thenThrowUnsupportedHostException() {
    assertThatThrownBy(() -> httpProvider.getUrl("http://unsupported-host:8000"))
        .isInstanceOf(UnsupportedHostException.class);
  }

  @Test
  public void givenAnInvalidJwtToken_whenGetUrlIsCalled_thenThrowInvalidTokenException() {
    when(authentication.getCredentials()).thenReturn("Invalid jwt token");

    assertThatThrownBy(() -> httpProvider.getUrl(CSS_STYLES_URL))
        .isInstanceOf(InvalidTokenException.class);
  }

  @Test
  public void givenGetUrlReturnsAnFSStream_whenGetReaderIsCalled_thenCharArrayReaderIsReturned() {
    when(authentication.getCredentials()).thenReturn(JWT_TOKEN);
    when(restTemplate
        .exchange(eq(CSS_STYLES_URL), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class)))
        .thenReturn(new ResponseEntity<>(CSS.getBytes(), HttpStatus.OK));

    FSStream fsStream = httpProvider.getUrl(CSS_STYLES_URL);

    assertThat(fsStream.getReader()).isInstanceOf(CharArrayReader.class);
  }

  @Test
  public void givenGetUrlReturnsAnFSStream_whenGetStreamIsCalled_thenByteArrayInputStreamIsReturned() {
    when(authentication.getCredentials()).thenReturn(JWT_TOKEN);
    when(restTemplate
        .exchange(eq(CSS_STYLES_URL), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class)))
        .thenReturn(new ResponseEntity<>(CSS.getBytes(), HttpStatus.OK));

    FSStream fsStream = httpProvider.getUrl(CSS_STYLES_URL);

    assertThat(fsStream.getStream()).isInstanceOf(ByteArrayInputStream.class);
  }
}
