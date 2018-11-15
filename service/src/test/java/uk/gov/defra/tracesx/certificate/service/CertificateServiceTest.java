package uk.gov.defra.tracesx.certificate.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Random;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.defra.tracesx.certificate.dao.entities.Certificate;
import uk.gov.defra.tracesx.certificate.service.blobstorage.BlobStorage;
import uk.gov.defra.tracesx.certificate.utillities.CertificatePDFGenerator;

public class CertificateServiceTest {

  @Mock CertificatePDFGenerator pdfGenerator;
  @Mock BlobStorage blobStorage;

  private CertificateService certificateService;

  @Before
  public void setUp() {
    initMocks(this);
    certificateService = new CertificateService(pdfGenerator, blobStorage);
  }

  @Test
  public void downloadCertificateReturnsExpectedByteArray() {
    String referenceNumber = UUID.randomUUID().toString();
    String etag = UUID.randomUUID().toString();

    byte[] document = new byte[20];
    new Random().nextBytes(document);

    Certificate expectedCertificate = new Certificate(referenceNumber, document);

    when(pdfGenerator.generate(any())).thenReturn(document);
    Certificate actualCertificate = certificateService.getCertificate(referenceNumber, etag);

    assertEquals(expectedCertificate, actualCertificate);
    assertEquals(actualCertificate.getDocument(), document);
  }

  @Test
  public void downloadCertificateCallsPdfGeneratorOnce() {
    String referenceNumber = UUID.randomUUID().toString();
    String etag = UUID.randomUUID().toString();

    when(pdfGenerator.generate(any())).thenReturn("Some Binary Data".getBytes());

    certificateService.getCertificate(referenceNumber, etag);

    verify(pdfGenerator, times(1)).generate(any());
  }
}
