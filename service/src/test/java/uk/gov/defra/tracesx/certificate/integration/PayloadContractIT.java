package uk.gov.defra.tracesx.certificate.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

/**
 * Pins every response the service sends, so that a framework or library upgrade can't change
 * them unnoticed: a plain certificate, a styled one that loads its stylesheet and image from the
 * frontend (a MockServer), and the requests the service rejects. The status, content type,
 * Content-Disposition and body are compared with {@code integration/payload/<name>.txt}.
 *
 * <p>PDFs are compared byte for byte (as ISO-8859-1, so every byte round-trips) after masking
 * what changes on every run: the creation and modification dates and the document ID. Error
 * bodies written by Spring Boot have their timestamp checked for format and then masked.
 *
 * <p>On a mismatch the actual response is written to {@code target/payload-actual}.
 */
class PayloadContractIT extends IntegrationBase {

  private static final String PAYLOAD_DIR = "integration/payload/";
  private static final String REFERENCE = "CHEDA.GB.2018.1234567";
  private static final Pattern PDF_DATE = Pattern.compile(
      "/(CreationDate|ModDate) ?\\(D:\\d{14}[^)]*\\)");
  private static final Pattern PDF_ID = Pattern.compile(
      "/ID ?\\[ ?<[0-9A-Fa-f]+> ?<[0-9A-Fa-f]+> ?\\]");
  private static final Pattern XMP_DATE = Pattern.compile(
      "<(xmp:\\w+Date)>[^<]+</(xmp:\\w+Date)>");
  private static final Pattern ERROR_TIMESTAMP = Pattern.compile(
      "\"timestamp\":\"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\+00:00\"");

  @Value("${frontendNotification.service.url}")
  private String frontendUrl;

  @BeforeEach
  void setUp() {
    givenUserHasPermissions("certificate.create");
  }

  @Test
  void plainCertificate() {
    send("plain", REFERENCE, "<p>hello world</p>", "");
  }

  @Test
  void styledCertificate() {
    usingStub()
        .when(request().withMethod("GET").withPath("/public/stylesheets/certificate.css"))
        .respond(response().withHeader("Content-Type", "text/css").withStatusCode(200)
            .withBody("body { font-family: 'Times New Roman'; } th { font-weight: bold; }"));
    usingStub()
        .when(request().withMethod("GET").withPath("/public/images/statuses/draft.png"))
        .respond(response().withHeader("Content-Type", "image/png").withStatusCode(200)
            .withBody("draft.png"));

    send("styled", REFERENCE, readResource("integration/certificate.html"), frontendUrl);
  }

  @Test
  void draftReference() {
    send("draft_reference", "DRAFT.GB.2026.1234567", "<p>draft</p>", "");
  }

  @Test
  void invalidReference() {
    send("invalid_reference", "CHEDA.INVALID.2018.1234567", "<p>hello world</p>", "");
  }

  @Test
  void invalidHtml() {
    send("invalid_html", REFERENCE, "<p hello world", "");
  }

  @Test
  void missingUrl() {
    EntityExchangeResult<byte[]> result = webClient("AnaAdams").post()
        .uri("/certificate/" + REFERENCE)
        .contentType(MediaType.TEXT_HTML)
        .bodyValue("<p>hello world</p>")
        .exchange().expectBody().returnResult();
    assertPayload("missing_url.txt", describe(result));
  }

  @Test
  void resourceOnAnotherHost() {
    send("resource_on_another_host", REFERENCE,
        "<html><head><link href=\"http://elsewhere.example/style.css\" rel=\"stylesheet\" />"
            + "</head><body><p>hello</p></body></html>",
        frontendUrl);
  }

  private void send(String name, String reference, String html, String url) {
    EntityExchangeResult<byte[]> result = webClient("AnaAdams").post()
        .uri("/certificate/" + reference + "?url=" + url)
        .contentType(MediaType.TEXT_HTML)
        .bodyValue(html)
        .exchange().expectBody().returnResult();
    assertPayload(name + ".txt", describe(result));
  }

  private static String describe(EntityExchangeResult<byte[]> result) {
    HttpHeaders headers = result.getResponseHeaders();
    byte[] content = result.getResponseBodyContent();
    String body = content == null ? "" : new String(content, StandardCharsets.ISO_8859_1);
    if (body.startsWith("%PDF")) {
      body = mask(PDF_DATE, body, "/$1 (<date>)");
      body = mask(PDF_ID, body, "/ID [<id>]");
      body = XMP_DATE.matcher(body).replaceAll("<$1><date></$2>");
    } else if (body.contains("\"timestamp\"")) {
      body = mask(ERROR_TIMESTAMP, body, "\"timestamp\":\"<timestamp>\"");
    }
    return "%d%nContent-Type: %s%nContent-Disposition: %s%n%n%s".formatted(
        result.getStatus().value(),
        headers.getContentType(),
        headers.getFirst(HttpHeaders.CONTENT_DISPOSITION),
        body);
  }

  private static String mask(Pattern pattern, String body, String replacement) {
    assertThat(body).containsPattern(pattern);
    return pattern.matcher(body).replaceAll(replacement);
  }

  private static String readResource(String name) {
    try (InputStream in = PayloadContractIT.class.getClassLoader().getResourceAsStream(name)) {
      if (in == null) {
        throw new IllegalStateException("Not on the test classpath: " + name);
      }
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException exception) {
      throw new IllegalStateException("Failed to read " + name, exception);
    }
  }

  private static void assertPayload(String expectedFile, String actual) {
    String expected = null;
    try (InputStream in = PayloadContractIT.class.getClassLoader()
        .getResourceAsStream(PAYLOAD_DIR + expectedFile)) {
      if (in != null) {
        expected = new String(in.readAllBytes(), StandardCharsets.ISO_8859_1);
      }
    } catch (IOException exception) {
      throw new IllegalStateException("Failed to read " + expectedFile, exception);
    }
    if (!actual.equals(expected)) {
      writeActual(expectedFile, actual);
    }
    assertThat(actual).as(expectedFile).isEqualTo(expected);
  }

  private static void writeActual(String name, String actual) {
    try {
      Path dir = Path.of("target", "payload-actual");
      Files.createDirectories(dir);
      Files.writeString(dir.resolve(name), actual, StandardCharsets.ISO_8859_1);
    } catch (IOException exception) {
      throw new IllegalStateException("Failed to write the actual " + name, exception);
    }
  }
}
