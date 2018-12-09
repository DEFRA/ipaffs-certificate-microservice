package uk.gov.defra.tracesx.certificate.dao.entities;

import java.util.Arrays;
import java.util.Objects;
import uk.gov.defra.tracesx.certificate.resource.ReferenceNumber;

public class Certificate {

  private ReferenceNumber referenceNumber;

  private byte[] document;

  public Certificate(ReferenceNumber referenceNumber, byte[] document) {
    this.referenceNumber = referenceNumber;
    this.document = document;
  }

  public Certificate() {}

  public ReferenceNumber getReferenceNumber() {
    return referenceNumber;
  }

  public void setReferenceNumber(ReferenceNumber referenceNumber) {
    this.referenceNumber = referenceNumber;
  }

  public byte[] getDocument() {
    return document;
  }

  public void setDocument(byte[] document) {
    this.document = document;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Certificate that = (Certificate) o;
    return Objects.equals(referenceNumber, that.referenceNumber)
        && Arrays.equals(document, that.document);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(referenceNumber);
    result = 31 * result + Arrays.hashCode(document);
    return result;
  }
}
