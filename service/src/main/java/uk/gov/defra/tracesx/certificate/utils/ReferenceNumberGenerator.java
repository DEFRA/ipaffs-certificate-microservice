package uk.gov.defra.tracesx.certificate.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.defra.tracesx.certificate.utils.exception.InvalidReferenceNumberException;

import java.util.Objects;
import java.util.regex.Pattern;

public class ReferenceNumberGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceNumberGenerator.class);
  private static final Pattern PATTERN =
      Pattern.compile(
          "((CHEDA|CHEDD|CHEDPP|CHEDP|DRAFT).GB.20\\d{2}.\\d{7,8})|(CHEDA|CHEDD|CHEDPP|CHEDP)");

  private final String referenceNumber;

  public static ReferenceNumberGenerator valueOf(String value) {
    return new ReferenceNumberGenerator(value);
  }

  public ReferenceNumberGenerator(String referenceNumber) {
    if (StringUtils.isBlank(referenceNumber) || !PATTERN.matcher(referenceNumber).matches()) {
      LOGGER.error("An invalid reference number was provided");
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
