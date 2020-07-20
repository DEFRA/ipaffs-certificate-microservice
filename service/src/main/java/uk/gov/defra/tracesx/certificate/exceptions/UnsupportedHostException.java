package uk.gov.defra.tracesx.certificate.exceptions;

public class UnsupportedHostException extends IllegalArgumentException {

  public UnsupportedHostException(String host) {
    super("Host not supported: " + host);
  }
}

