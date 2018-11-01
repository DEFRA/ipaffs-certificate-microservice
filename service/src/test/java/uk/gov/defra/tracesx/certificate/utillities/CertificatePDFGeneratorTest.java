package uk.gov.defra.tracesx.certificate.utillities;

import java.io.IOException;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import uk.gov.defra.tracesx.certificate.dao.entities.Certificate;


public class CertificatePDFGeneratorTest {

  private CertificatePDFGenerator pdfGenerator;
  private Certificate certificate;
  private final UUID certificateUUID = UUID.randomUUID();

  @Before
  public void setUp() throws IOException {
    certificate = new Certificate();
    certificate.setId(certificateUUID);
    certificate.setId(UUID.randomUUID());
    pdfGenerator = new CertificatePDFGenerator();
  }

  @Test
  public void shouldGenerateExpectedPDF(){
    byte[] pdfFile = pdfGenerator.generate(certificate);
  }
}
