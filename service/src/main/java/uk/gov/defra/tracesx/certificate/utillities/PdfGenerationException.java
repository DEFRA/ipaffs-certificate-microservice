package uk.gov.defra.tracesx.certificate.utillities;

public class PdfGenerationException extends RuntimeException {

  public PdfGenerationException(String msg, Exception cause) {
    super(msg, cause);
  }
}
