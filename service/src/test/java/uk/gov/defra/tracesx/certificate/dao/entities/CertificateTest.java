package uk.gov.defra.tracesx.certificate.dao.entities;

import static org.junit.Assert.assertEquals;

import java.util.UUID;
import org.junit.Before;
import org.junit.Test;

public class CertificateTest {

  @Before
  public void setUp() {}

  @Test
  public void verifyReferenceProperty() {
    Certificate instance = new Certificate();
    String referenceNumber = UUID.randomUUID().toString();

    instance.setReferenceNumber(referenceNumber);

    assertEquals(referenceNumber, instance.getReferenceNumber());
  }

  @Test
  public void verifyDocumentProperty() {
    Certificate instance = new Certificate();
    byte[] document = "Some data to be stored as binary".getBytes();

    instance.setDocument(document);

    assertEquals(document, instance.getDocument());
  }
}
