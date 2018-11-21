package uk.gov.defra.tracesx.certificate.utillities;

public class FontNotFoundException extends RuntimeException {

  public FontNotFoundException(String filename) {
    super("Font not found for: " + filename);
  }
}
