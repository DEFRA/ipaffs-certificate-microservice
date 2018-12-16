package uk.gov.defra.tracesx.certificate.utilities;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import org.junit.Test;

public class UriParserTest {

  private UriParser uriParser = new UriParser();

  @Test
  public void shouldReturnBaseUri() {
    assertBaseUri("http://www.any.com/some/path", "http://www.any.com");
    assertBaseUri("http://www.any.com:8080/some/path", "http://www.any.com:8080");
    assertBaseUri("https://www.any.com:8080/some/path", "https://www.any.com:8080");
    assertBaseUri("https://www.any.com/some/path", "https://www.any.com");
    assertBaseUri("http://www.any.com/some/path", "http://www.any.com");
  }

  private void assertBaseUri(String url, String expected) {
    final URI baseUri = uriParser.getBaseUri(URI.create(url));
    assertThat(baseUri.toString()).isEqualTo(expected);
  }
}