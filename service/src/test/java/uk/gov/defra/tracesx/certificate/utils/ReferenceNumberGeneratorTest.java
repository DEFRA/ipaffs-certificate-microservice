package uk.gov.defra.tracesx.certificate.utils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import uk.gov.defra.tracesx.certificate.utils.exception.InvalidReferenceNumberException;

public class ReferenceNumberGeneratorTest {

  private final String CVEDA_REFERENCE = "CHEDA.GB.2018.12345678";
  private final String INVALID_REFERENCE_REF_NUM_BREACH_TOO_LONG = "CHEDA.GB.2018.12345678213123";
  private final String INVALID_REFERENCE_REF_NUM_BREACH_TOO_SHORT = "CHEDP.GB.2018.123";

  @Test
  public void shouldCreateReferenceNumberForCVEDA_WhenBlank() {
    String CVEDA_BLANK_REFERENCE = "CHEDA";
    assertThat(ReferenceNumberGenerator.valueOf(CVEDA_BLANK_REFERENCE).valueOf())
        .isEqualTo(CVEDA_BLANK_REFERENCE);
  }

  @Test
  public void shouldCreateReferenceNumberForCVEDP_WhenBlank() {
    String CVEDP_BLANK_REFERENCE = "CHEDP";
    assertThat(ReferenceNumberGenerator.valueOf(CVEDP_BLANK_REFERENCE).valueOf())
        .isEqualTo(CVEDP_BLANK_REFERENCE);
  }

  @Test
  public void shouldCreateReferenceNumberForCHEDPP_WhenBlank() {
    String CHEDPP_BLANK_REFERENCE = "CHEDPP";
    assertThat(ReferenceNumberGenerator.valueOf(CHEDPP_BLANK_REFERENCE).valueOf())
        .isEqualTo(CHEDPP_BLANK_REFERENCE);
  }

  @Test
  public void shouldCreateReferenceNumberForCED_WhenBlank() {
    String CED_BLANK_REFERENCE = "CHEDD";
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
    String CVEDP_REFERENCE = "CHEDP.GB.2018.1234567";
    assertThat(ReferenceNumberGenerator.valueOf(CVEDP_REFERENCE).valueOf())
        .isEqualTo(CVEDP_REFERENCE);
  }

  @Test
  public void shouldCreateReferenceNumberForCHEDPP() {
    String CHEDPP_REFERENCE = "CHEDPP.GB.2018.1234567";
    assertThat(ReferenceNumberGenerator.valueOf(CHEDPP_REFERENCE).valueOf())
        .isEqualTo(CHEDPP_REFERENCE);
  }

  @Test
  public void shouldCreateReferenceNumberForCED() {
    String CED_REFERENCE = "CHEDD.GB.2018.1234567";
    assertThat(ReferenceNumberGenerator.valueOf(CED_REFERENCE).valueOf()).isEqualTo(CED_REFERENCE);
  }

  @Test
  public void shouldCreateReferenceNumberForDraftStatus() {
    String DRAFT_REFERENCE = "DRAFT.GB.2018.1234567";
    assertThat(ReferenceNumberGenerator.valueOf(DRAFT_REFERENCE).valueOf())
        .isEqualTo(DRAFT_REFERENCE);
  }

  @Test
  public void shouldSupportToString() {
    assertThat(ReferenceNumberGenerator.valueOf(CVEDA_REFERENCE)).hasToString(
        "ReferenceNumberGenerator(" + CVEDA_REFERENCE + ")");
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
    assertThatThrownBy(
        () -> ReferenceNumberGenerator.valueOf(INVALID_REFERENCE_REF_NUM_BREACH_TOO_LONG))
        .isInstanceOf(InvalidReferenceNumberException.class);

    assertThatThrownBy(
        () -> ReferenceNumberGenerator.valueOf(INVALID_REFERENCE_REF_NUM_BREACH_TOO_SHORT))
        .isInstanceOf(InvalidReferenceNumberException.class);
  }

  @Test
  public void equalsAndHashCode() {
    EqualsVerifier.forClass(ReferenceNumberGenerator.class).usingGetClass().verify();
  }
}
