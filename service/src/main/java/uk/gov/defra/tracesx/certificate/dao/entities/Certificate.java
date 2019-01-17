package uk.gov.defra.tracesx.certificate.dao.entities;

import java.util.Arrays;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;
import uk.gov.defra.tracesx.certificate.resource.ReferenceNumber;

@Getter
@Setter
public class Certificate {

  private ReferenceNumber referenceNumber;

  private byte[] document;

  public Certificate(ReferenceNumber referenceNumber, byte[] document) {
    this.referenceNumber = referenceNumber;
    this.document = document;
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(referenceNumber);
    result = 31 * result + Arrays.hashCode(document);
    return result;
  }
}
