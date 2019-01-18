package uk.gov.defra.tracesx.certificate.dao.entities;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.defra.tracesx.certificate.resource.ReferenceNumber;

import java.util.Arrays;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class CertificateTest {

  private static final ReferenceNumber REFERENCE_NUMBER = new ReferenceNumber("CVEDA.GB.2019.0000000");
  private static final byte[] DOCUMENT = "string".getBytes();

  @Test
  public void hasCode_ReturnsCorrectResult() {
    int expected = 31 * Objects.hash(REFERENCE_NUMBER) + Arrays.hashCode(DOCUMENT);
    Certificate certificate = new Certificate(REFERENCE_NUMBER, DOCUMENT);
    assertEquals(expected, certificate.hashCode());
  }
}
