package uk.gov.defra.tracesx.certificate.utilities.exception;

public class PdfGenerationException extends RuntimeException {

  public PdfGenerationException(String msg, Exception cause) {
    super(msg, cause);
  }
}
