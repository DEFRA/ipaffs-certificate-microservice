package uk.gov.defra.tracesx.certificate.service.blobstorage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import uk.gov.defra.tracesx.certificate.dao.entities.Certificate;

@Repository
public class BlobStorage implements Storage {

  private Map<BlobStorageIdentifier, Certificate> blobStorageHashMap = new HashMap<>();
  //  This will be replaced by actual Blob storage once it is available

  @Override
  public boolean storeCertificate(BlobStorageIdentifier key, Certificate certificate) {
    if (!certificateExists(key)) {
      blobStorageHashMap.put(key, certificate);
      return true;
    }
    return false;
  }

  @Override
  public Optional<Certificate> getCertificate(BlobStorageIdentifier key) {
    // return Optional.ofNullable(blobStorageHashMap.get(key));
    // returning an empty so certificate can be generated on each request as per latest comments
    // from stu on IMTA-2478
    return Optional.empty();
  }

  @Override
  public boolean certificateExists(BlobStorageIdentifier key) {
    return blobStorageHashMap.containsKey(key);
  }
}
