package uk.gov.defra.tracesx.certificate.service.blobstorage;

import javax.validation.constraints.NotNull;

public class BlobStorageIdentifier {
  private final String referenceNumber;
  private final String etag;

  public BlobStorageIdentifier(@NotNull String referenceNumber, @NotNull String etag) {
    this.referenceNumber = referenceNumber;
    this.etag = etag;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof BlobStorageIdentifier) {
      BlobStorageIdentifier s = (BlobStorageIdentifier) obj;
      return referenceNumber.equals(s.referenceNumber) && etag.equals(s.etag);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return (referenceNumber + etag).hashCode();
  }
}
