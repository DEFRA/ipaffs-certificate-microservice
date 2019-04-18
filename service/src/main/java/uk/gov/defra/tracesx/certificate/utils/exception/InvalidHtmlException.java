package uk.gov.defra.tracesx.certificate.utils.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidHtmlException extends RuntimeException {

  public InvalidHtmlException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
