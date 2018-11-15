package uk.gov.defra.tracesx.certificate.service.blobstorage;

import java.util.Optional;
import uk.gov.defra.tracesx.certificate.dao.entities.Certificate;

public interface Storage {
  boolean storeCertificate(BlobStorageIdentifier key, Certificate certificate);

  Optional<Certificate> getCertificate(BlobStorageIdentifier key);

  boolean certificateExists(BlobStorageIdentifier key);
}
