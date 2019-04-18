package uk.gov.defra.tracesx.certificate.utils.exception;

public class PdfGenerationException extends RuntimeException {

  public PdfGenerationException(String msg, Exception cause) {
    super(msg, cause);
  }
}
