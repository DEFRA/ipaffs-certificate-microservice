package uk.gov.defra.tracesx.certificate.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import org.junit.jupiter.api.Test;

public class UriParserTest {

  private UriParser uriParser = new UriParser();

  @Test
  public void shouldReturnBaseUri() {
    assertBaseUri("http://www.somewebsite.com/some/path", "http://www.somewebsite.com");
    assertBaseUri("http://www.somewebsite.com:8080/some/path", "http://www.somewebsite.com:8080");
    assertBaseUri("https://www.somewebsite.com:8080/some/path", "https://www.somewebsite.com:8080");
    assertBaseUri("https://www.somewebsite.com/some/path", "https://www.somewebsite.com");
    assertBaseUri("http://www.somewebsite.com/some/path", "http://www.somewebsite.com");
    assertBaseUri("//www.somewebsite.com:8080", "http://www.somewebsite.com:8080");
  }

  private void assertBaseUri(String url, String expected) {
    final URI baseUri = uriParser.getBaseUri(URI.create(url));
    assertThat(baseUri.toString()).isEqualTo(expected);
  }
}
