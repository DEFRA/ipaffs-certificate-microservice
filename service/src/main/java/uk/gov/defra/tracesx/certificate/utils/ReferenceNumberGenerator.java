package uk.gov.defra.tracesx.certificate.utils;

import org.apache.commons.lang3.StringUtils;
import uk.gov.defra.tracesx.certificate.utils.exception.InvalidReferenceNumberException;

import java.util.Objects;
import java.util.regex.Pattern;

public class ReferenceNumberGenerator {

  private static final Pattern PATTERN =
      Pattern.compile("(CVEDA|CED|CVEDP|DRAFT).GB.20\\d{2}.\\d{7,8}");

  private final String referenceNumber;

  public static final ReferenceNumberGenerator valueOf(String value) {
    return new ReferenceNumberGenerator(value);
  }

  public ReferenceNumberGenerator(String referenceNumber) {
    if (StringUtils.isBlank(referenceNumber) || !PATTERN.matcher(referenceNumber).matches()) {
      throw new InvalidReferenceNumberException(referenceNumber);
    }
    this.referenceNumber = referenceNumber;
  }

  public String valueOf() {
    return referenceNumber;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    ReferenceNumberGenerator that = (ReferenceNumberGenerator) obj;
    return Objects.equals(referenceNumber, that.referenceNumber);
  }

  @Override
  public int hashCode() {
    return Objects.hash(referenceNumber);
  }

  @Override
  public String toString() {
    return String.format("ReferenceNumberGenerator(%s)", referenceNumber);
  }
}
