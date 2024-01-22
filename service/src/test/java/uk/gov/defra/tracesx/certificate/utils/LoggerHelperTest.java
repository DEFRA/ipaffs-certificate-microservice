package uk.gov.defra.tracesx.certificate.utils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.defra.tracesx.certificate.utils.LoggerHelper.replaceNewLines;

import org.junit.jupiter.api.Test;

public class LoggerHelperTest {

  @Test
  public void replacesCarriageReturnCharactersWithUnderscores() {
    String input = "A\treturned\rtest\nstring";
    String expected = "A_returned_test_string";

    assertThat(replaceNewLines(input)).isEqualTo(expected);
  }
}
