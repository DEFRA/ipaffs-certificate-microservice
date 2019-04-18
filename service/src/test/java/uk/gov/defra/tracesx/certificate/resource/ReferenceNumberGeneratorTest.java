package uk.gov.defra.tracesx.certificate.resource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import org.junit.Test;
import uk.gov.defra.tracesx.certificate.utils.ReferenceNumberGenerator;
import uk.gov.defra.tracesx.certificate.utils.exception.InvalidReferenceNumberException;

public class ReferenceNumberGeneratorTest {

  private final String CVEDA_REFERENCE = "CVEDA.GB.2018.1234567";
  private final String CVEDP_REFERENCE = "CVEDP.GB.2018.1234567";
  private final String CED_REFERENCE = "CED.GB.2018.1234567";
  private final String DRAFT_REFERENCE = "DRAFT.GB.2018.1234567";

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
  public void shouldSupportToString() throws Exception {
    assertThat(ReferenceNumberGenerator.valueOf(CVEDA_REFERENCE).toString())
        .isEqualTo("ReferenceNumberGenerator(CVEDA.GB.2018.1234567)");
  }

  @Test
  public void shouldThrowInvalidReferenceNumberException() {
    assertThatThrownBy(() -> ReferenceNumberGenerator.valueOf("CVE"))
        .isInstanceOf(InvalidReferenceNumberException.class)
        .hasMessage("An invalid reference number was provided: CVE");
  }

  @Test
  public void shouldThrowExceptionIfNull() throws Exception {
    assertThatThrownBy(() -> ReferenceNumberGenerator.valueOf(null))
        .isInstanceOf(InvalidReferenceNumberException.class);
  }

  @Test
  public void shouldThrowExceptionIfEmpty() throws Exception {
    assertThatThrownBy(() -> ReferenceNumberGenerator.valueOf(""))
        .isInstanceOf(InvalidReferenceNumberException.class);
  }

  @Test
  public void shouldThrowExceptionIfBlank() throws Exception {
    assertThatThrownBy(() -> ReferenceNumberGenerator.valueOf("   "))
        .isInstanceOf(InvalidReferenceNumberException.class);

  }
}
