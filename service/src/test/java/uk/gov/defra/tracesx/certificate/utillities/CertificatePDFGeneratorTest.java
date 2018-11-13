package uk.gov.defra.tracesx.certificate.utillities;

import java.util.UUID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CertificatePDFGeneratorTest {

  private CertificatePDFGenerator pdfGenerator;
  private final String referenceNumber = UUID.randomUUID().toString();

  @Before
  public void setUp() {
    pdfGenerator = new CertificatePDFGenerator();
  }

  @Test
  public void shouldGeneratePDF() {
    byte[] generatedPdfBinaryData = pdfGenerator.generate(referenceNumber);
    Assert.assertNotNull(generatedPdfBinaryData);
  }
}
