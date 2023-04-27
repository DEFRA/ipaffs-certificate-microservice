package uk.gov.defra.tracesx.certificate.utils;

import com.openhtmltopdf.extend.FSStream;
import com.openhtmltopdf.extend.FSStreamFactory;
import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.defra.tracesx.certificate.exceptions.InvalidTokenException;
import uk.gov.defra.tracesx.certificate.exceptions.UnsupportedHostException;

@Component
public class PdfHttpProvider implements FSStreamFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(PdfHttpProvider.class);
  private static final String VALID_JWT_REGEX =
      "^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$";

  private final RestTemplate restTemplate;

  @Value("${frontendNotification.service.host}")
  private String host;

  public PdfHttpProvider(RestTemplate httpClient) {
    this.restTemplate = httpClient;
  }

  @Override
  public FSStream getUrl(String uri) {

    LOGGER.info("calling: {}", uri);
    validateUri(uri);
    HttpHeaders headers = new HttpHeaders();
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    String token = (String) authentication.getCredentials();
    if (!token.matches(VALID_JWT_REGEX)) {
      throw new InvalidTokenException("Invalid JWT token");
    }

    headers.add("Authorization", "Bearer " + token);
    HttpEntity<?> requestEntity = new HttpEntity<>(headers);
    ResponseEntity<byte[]> exchange =
        restTemplate.exchange(uri, HttpMethod.GET, requestEntity, byte[].class);
    return new FSStream() {

      @Override
      public InputStream getStream() {
        return new ByteArrayInputStream(exchange.getBody());
      }

      @Override
      public Reader getReader() {
        String text = new String(exchange.getBody(), StandardCharsets.UTF_8);
        return new CharArrayReader(text.toCharArray());
      }
    };
  }

  private void validateUri(String uri) {
    String uriHost = URI.create(uri).getHost();
    if (!uriHost.equals(this.host)) {
      throw new UnsupportedHostException(uriHost);
    }
  }
}
