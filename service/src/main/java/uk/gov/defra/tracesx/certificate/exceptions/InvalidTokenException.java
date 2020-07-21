package uk.gov.defra.tracesx.certificate.exceptions;

public class InvalidTokenException extends RuntimeException {

  public InvalidTokenException(String message) {
    super(message);
  }
}

