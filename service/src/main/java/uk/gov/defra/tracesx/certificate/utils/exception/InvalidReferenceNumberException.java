package uk.gov.defra.tracesx.certificate.utils.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidReferenceNumberException extends RuntimeException {

  public InvalidReferenceNumberException(String referenceNumber) {
    super("An invalid reference number was provided: " + referenceNumber);
  }
}
