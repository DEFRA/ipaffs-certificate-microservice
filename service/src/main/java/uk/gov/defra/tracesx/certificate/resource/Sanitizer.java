package uk.gov.defra.tracesx.certificate.resource;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sanitizer {

  private static final Logger LOG = LoggerFactory.getLogger(Sanitizer.class);

  private static final PolicyFactory POLICY = new HtmlPolicyBuilder()
      .allowElements("main", "html", "head", "title", "link", "body")
      .allowAttributes("href", "rel").onElements("link")
      .allowAttributes("colspan").globally()
      .allowAttributes("class").globally()
      .toFactory()
      .and(Sanitizers.FORMATTING)
      .and(Sanitizers.IMAGES)
      .and(Sanitizers.TABLES)
      .and(Sanitizers.BLOCKS);

  private Sanitizer() {
    // no-arg constructor
  }

  public static final String sanitize(String unsafeHtml) {
    LOG.debug(unsafeHtml);
    String safeHtml = POLICY.sanitize(unsafeHtml);
    LOG.debug(safeHtml);
    return safeHtml;
  }
}
