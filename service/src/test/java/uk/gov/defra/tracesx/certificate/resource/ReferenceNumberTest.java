package uk.gov.defra.tracesx.certificate.resource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.Test;
import uk.gov.defra.tracesx.certificate.utilities.exception.InvalidReferenceNumberException;

public class ReferenceNumberTest {

  private final String VALID_REFERENCE = "CVEDA.GB.2018.1234567";

  @Test
  public void shouldCreateReferenceNumber() {
    assertThat(ReferenceNumber.valueOf(VALID_REFERENCE).valueOf())
        .isEqualTo(VALID_REFERENCE);
  }

  @Test
  public void shouldSupportToString() throws Exception {
    assertThat(ReferenceNumber.valueOf(VALID_REFERENCE).toString())
        .isEqualTo("ReferenceNumber(CVEDA.GB.2018.1234567)");
  }

  @Test
  public void shouldThrowInvalidReferenceNumberException() {
    assertThatThrownBy(() -> ReferenceNumber.valueOf("CVE"))
        .isInstanceOf(InvalidReferenceNumberException.class)
        .hasMessage("An invalid reference number was provided: CVE");
  }

  @Test
  public void shouldThrowExceptionIfNull() throws Exception {
    assertThatThrownBy(() -> ReferenceNumber.valueOf(null))
        .isInstanceOf(InvalidReferenceNumberException.class);
  }

  @Test
  public void shouldThrowExceptionIfEmpty() throws Exception {
    assertThatThrownBy(() -> ReferenceNumber.valueOf(""))
        .isInstanceOf(InvalidReferenceNumberException.class);
  }

  @Test
  public void shouldThrowExceptionIfBlank() throws Exception {
    assertThatThrownBy(() -> ReferenceNumber.valueOf("   "))
        .isInstanceOf(InvalidReferenceNumberException.class);

  }
}
