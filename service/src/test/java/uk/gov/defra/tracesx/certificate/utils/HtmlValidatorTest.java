package uk.gov.defra.tracesx.certificate.utils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import uk.gov.defra.tracesx.certificate.utils.exception.InvalidHtmlException;

public class HtmlValidatorTest {

  @Test
  public void shouldAllowValidHtml() throws Exception {
    HtmlValidator.validate("<html><body></body></html>");
  }

  @Test
  public void shouldThrowInvalidHtmlExceptionOnUnclosedTags() {
    assertThatThrownBy(() -> HtmlValidator.validate("<html><body></body</html>"))
        .isInstanceOf(InvalidHtmlException.class)
        .hasMessage("Invalid html was provided");
  }

  @Test
  public void shouldThrowInvalidHtmlExceptionOnMalformedTag() {
    assertThatThrownBy(() -> HtmlValidator.validate("<html body"))
        .isInstanceOf(InvalidHtmlException.class)
        .hasMessage("Invalid html was provided");
  }

  @Test
  public void shouldThrowInvalidHtmlExceptionOnNonHtml() {
    assertThatThrownBy(() -> HtmlValidator.validate("invalid"))
        .isInstanceOf(InvalidHtmlException.class)
        .hasMessage("Invalid html was provided");
  }
}
