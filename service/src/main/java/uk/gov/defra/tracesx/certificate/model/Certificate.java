package uk.gov.defra.tracesx.certificate.model;

import lombok.Getter;
import lombok.Setter;
import uk.gov.defra.tracesx.certificate.utils.ReferenceNumberGenerator;

import java.util.Arrays;
import java.util.Objects;

@Getter
@Setter
public class Certificate {

  private ReferenceNumberGenerator referenceNumber;

  private byte[] document;

  public Certificate(ReferenceNumberGenerator referenceNumber, byte[] document) {
    this.referenceNumber = referenceNumber;
    this.document = document;
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(referenceNumber);
    result = 31 * result + Arrays.hashCode(document);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    Certificate that = (Certificate) obj;
    return Objects.equals(referenceNumber, that.referenceNumber)
        && Arrays.equals(document, that.document);
  }
}