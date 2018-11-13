package uk.gov.defra.tracesx.certificate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.defra.tracesx.certificate.dao.entities.Certificate;
import uk.gov.defra.tracesx.certificate.service.blobstorage.BlobStorage;
import uk.gov.defra.tracesx.certificate.service.blobstorage.BlobStorageIdentifier;
import uk.gov.defra.tracesx.certificate.utillities.CertificatePDFGenerator;

@Service
public class CertificateService {

  private final CertificatePDFGenerator pdfGenerator;
  private final BlobStorage storage;

  @Autowired
  public CertificateService(CertificatePDFGenerator pdfGenerator, BlobStorage storage) {
    this.pdfGenerator = pdfGenerator;
    this.storage = storage;
  }

  public Certificate getCertificate(String referenceNumber, String etag) {
    BlobStorageIdentifier key = new BlobStorageIdentifier(referenceNumber, etag);
    return storage.getCertificate(key).orElseGet(() -> createCertificate(referenceNumber, key));
  }

  private Certificate createCertificate(String referenceNumber, BlobStorageIdentifier key) {
    byte[] bytes = pdfGenerator.generate(referenceNumber);
    Certificate certificate = new Certificate(referenceNumber, bytes);
    storeCertificate(key, certificate);
    return certificate;
  }

  private void storeCertificate(BlobStorageIdentifier key, Certificate certificate) {
    storage.storeCertificate(key, certificate);
  }
}
