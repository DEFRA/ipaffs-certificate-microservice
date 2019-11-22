package uk.gov.defra.tracesx.certificate.resource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import org.junit.Test;
import uk.gov.defra.tracesx.certificate.utils.ReferenceNumberGenerator;
import uk.gov.defra.tracesx.certificate.utils.exception.InvalidReferenceNumberException;

public class ReferenceNumberGeneratorTest {

  private final String CVEDA_BLANK_REFERENCE = "CHEDA";
  private final String CVEDP_BLANK_REFERENCE = "CHEDP";
  private final String CED_BLANK_REFERENCE = "CHEDD";
  private final String CVEDA_REFERENCE = "CHEDA.GB.2018.12345678";
  private final String CVEDP_REFERENCE = "CHEDP.GB.2018.1234567";
  private final String CED_REFERENCE = "CHEDD.GB.2018.1234567";
  private final String DRAFT_REFERENCE = "DRAFT.GB.2018.1234567";
  private final String INVALID_REFERENCE_REF_NUM_BREACH_TOO_LONG = "CHEDA.GB.2018.12345678213123";
  private final String INVALID_REFERENCE_REF_NUM_BREACH_TOO_SHORT = "CHEDP.GB.2018.123";

  @Test
  public void shouldCreateReferenceNumberForCVEDA_WhenBlank() {
    assertThat(ReferenceNumberGenerator.valueOf(CVEDA_BLANK_REFERENCE).valueOf())
        .isEqualTo(CVEDA_BLANK_REFERENCE);
  }

  @Test
  public void shouldCreateReferenceNumberForCVEDP_WhenBlank() {
    assertThat(ReferenceNumberGenerator.valueOf(CVEDP_BLANK_REFERENCE).valueOf())
        .isEqualTo(CVEDP_BLANK_REFERENCE);
  }

  @Test
  public void shouldCreateReferenceNumberForCED_WhenBlank() {
    assertThat(ReferenceNumberGenerator.valueOf(CED_BLANK_REFERENCE).valueOf())
        .isEqualTo(CED_BLANK_REFERENCE);
  }

  @Test
  public void shouldCreateReferenceNumberForCVEDA() {
    assertThat(ReferenceNumberGenerator.valueOf(CVEDA_REFERENCE).valueOf())
        .isEqualTo(CVEDA_REFERENCE);
  }

  @Test
  public void shouldCreateReferenceNumberForCVEDP() {
    assertThat(ReferenceNumberGenerator.valueOf(CVEDP_REFERENCE).valueOf())
        .isEqualTo(CVEDP_REFERENCE);
  }

  @Test
  public void shouldCreateReferenceNumberForCED() {
    assertThat(ReferenceNumberGenerator.valueOf(CED_REFERENCE).valueOf())
        .isEqualTo(CED_REFERENCE);
  }

  @Test
  public void shouldCreateReferenceNumberForDraftStatus() {
    assertThat(ReferenceNumberGenerator.valueOf(DRAFT_REFERENCE).valueOf())
        .isEqualTo(DRAFT_REFERENCE);
  }

  @Test
  public void shouldSupportToString() {
    assertThat(ReferenceNumberGenerator.valueOf(CVEDA_REFERENCE).toString())
        .isEqualTo("ReferenceNumberGenerator(" + CVEDA_REFERENCE + ")");
  }

  @Test
  public void shouldThrowInvalidReferenceNumberException() {
    assertThatThrownBy(() -> ReferenceNumberGenerator.valueOf("CVE"))
        .isInstanceOf(InvalidReferenceNumberException.class)
        .hasMessage("An invalid reference number was provided: CVE");
  }

  @Test
  public void shouldThrowExceptionIfNull() {
    assertThatThrownBy(() -> ReferenceNumberGenerator.valueOf(null))
        .isInstanceOf(InvalidReferenceNumberException.class);
  }

  @Test
  public void shouldThrowExceptionIfEmpty() {
    assertThatThrownBy(() -> ReferenceNumberGenerator.valueOf(""))
        .isInstanceOf(InvalidReferenceNumberException.class);
  }

  @Test
  public void shouldThrowExceptionIfBlank() {
    assertThatThrownBy(() -> ReferenceNumberGenerator.valueOf("   "))
        .isInstanceOf(InvalidReferenceNumberException.class);

  }

  @Test
  public void shouldThrowExceptionIfRangeBreached() {
    assertThatThrownBy(() -> ReferenceNumberGenerator.valueOf(INVALID_REFERENCE_REF_NUM_BREACH_TOO_LONG))
        .isInstanceOf(InvalidReferenceNumberException.class);

    assertThatThrownBy(() -> ReferenceNumberGenerator.valueOf(INVALID_REFERENCE_REF_NUM_BREACH_TOO_SHORT))
        .isInstanceOf(InvalidReferenceNumberException.class);
  }
}
