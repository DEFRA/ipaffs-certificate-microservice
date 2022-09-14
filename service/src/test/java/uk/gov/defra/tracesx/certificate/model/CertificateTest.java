package uk.gov.defra.tracesx.certificate.model;

import static nl.jqno.equalsverifier.Warning.NONFINAL_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import uk.gov.defra.tracesx.certificate.utils.ReferenceNumberGenerator;

class CertificateTest {

  @Test
  void equalsAndHashCode() {
    EqualsVerifier.forClass(Certificate.class).usingGetClass().suppress(NONFINAL_FIELDS).verify();
  }

  @Test
  void constructor_WhenPropertiesProvided_ThenPropertiesAreSet() {
    byte[] DOCUMENT = "string".getBytes();
    ReferenceNumberGenerator REFERENCE_NUMBER = new ReferenceNumberGenerator(
        "CHEDA.GB.2019.0000000");

    //When
    Certificate certificate = new Certificate(REFERENCE_NUMBER, DOCUMENT);

    //Then
    assertThat(certificate.getDocument()).isEqualTo(DOCUMENT);
    assertThat(certificate.getReferenceNumber()).isEqualTo(REFERENCE_NUMBER);
  }
}