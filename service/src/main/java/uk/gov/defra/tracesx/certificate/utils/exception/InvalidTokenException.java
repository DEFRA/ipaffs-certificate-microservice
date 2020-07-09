package uk.gov.defra.tracesx.certificate.utils.exception;

public class InvalidTokenException extends RuntimeException {

  public InvalidTokenException(String msg) {
    super(msg);
  }
}
