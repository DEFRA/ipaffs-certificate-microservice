package uk.gov.defra.tracesx.certificate.service.blobstorage;

import static org.mockito.MockitoAnnotations.initMocks;

import java.util.UUID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.gov.defra.tracesx.certificate.dao.entities.Certificate;

public class BlobStorageTest {

  private Storage storage;
  private BlobStorageIdentifier key;
  private Certificate certificate;
  private String referenceNumber;
  private String etag;

  @Before
  public void setUp() {
    initMocks(this);
    storage = new BlobStorage();
    referenceNumber = UUID.randomUUID().toString();
    etag = UUID.randomUUID().toString();
    key = new BlobStorageIdentifier(referenceNumber, etag);
    certificate = new Certificate();
    certificate.setReferenceNumber(referenceNumber);
    certificate.setDocument("Some binary data".getBytes());
  }

  @Test
  public void testStoreCertificate() {
    storage.storeCertificate(key, certificate);
    // Assert.assertEquals(certificate, storage.getCertificate(key).get());
  }

  @Test
  public void testGetCertificate() {
    storage.storeCertificate(key, certificate);
    // Assert.assertEquals(certificate, storage.getCertificate(key).get());
  }

  @Test
  public void testIsCertificateExists() {
    storage.storeCertificate(key, certificate);
    // Assert.assertTrue(storage.getCertificate(key).isPresent());
    Assert.assertFalse(storage.getCertificate(key).isPresent());
    // getCertificate always returning empty as per latest changes made
    // in line with stu comments

  }
}
