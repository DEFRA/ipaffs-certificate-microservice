package uk.gov.defra.tracesx.certificate.dao.entities;

import java.util.Arrays;
import java.util.Objects;

public class Certificate {

  private String referenceNumber;

  private byte[] document;

  public Certificate() {}

  public String getReferenceNumber() {
    return referenceNumber;
  }

  public void setReferenceNumber(String referenceNumber) {
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

  public Certificate(String referenceNumber, byte[] document) {
    this.referenceNumber = referenceNumber;
    this.document = document;
  }
}
