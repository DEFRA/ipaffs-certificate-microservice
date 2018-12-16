package uk.gov.defra.tracesx.certificate.utilities;

import java.net.URI;
import org.springframework.stereotype.Component;

@Component
public class UriParser {

  public URI getBaseUri(URI uri) {
    return URI.create(scheme(uri) + uri.getHost() + port(uri));
  }

  private String scheme(URI uri) {
    return uri.getScheme() == null ? "http://" : uri.getScheme() + "://";
  }

  private String port(URI uri) {
    return uri.getPort() == -1 ? "" : ":" + uri.getPort();
  }
}
