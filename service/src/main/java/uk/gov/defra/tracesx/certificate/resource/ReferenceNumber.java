package uk.gov.defra.tracesx.certificate.resource;

import java.util.Objects;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import uk.gov.defra.tracesx.certificate.utillities.InvalidReferenceNumberException;

public class ReferenceNumber {

  private static final Pattern PATTERN = Pattern.compile("(CVEDA|CED|CVEDP).GB.20\\d{2}.\\d{7}");

  private final String referenceNumber;

  public static final ReferenceNumber valueOf(String value) {
    return new ReferenceNumber(value);
  }

  public ReferenceNumber(String referenceNumber) {
    if(StringUtils.isBlank(referenceNumber) || !PATTERN.matcher(referenceNumber).matches()) {
      throw new InvalidReferenceNumberException(referenceNumber);
    }
    this.referenceNumber = referenceNumber;
  }

  public String valueOf() {
    return referenceNumber;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReferenceNumber that = (ReferenceNumber) o;
    return Objects.equals(referenceNumber, that.referenceNumber);
  }

  @Override
  public int hashCode() {
    return Objects.hash(referenceNumber);
  }

  @Override
  public String toString() {
    return String.format("ReferenceNumber(%s)", referenceNumber);
  }
}
