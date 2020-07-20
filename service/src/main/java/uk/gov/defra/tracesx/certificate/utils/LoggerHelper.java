package uk.gov.defra.tracesx.certificate.utils;

import org.springframework.stereotype.Component;

@Component
public final class LoggerHelper {

  private static final String CARRIAGE_RETURN_REGEX = "[\n|\r|\t]";

  private LoggerHelper() {
  }

  public static String replaceNewLines(String inputString) {
    return inputString.replaceAll(CARRIAGE_RETURN_REGEX, "_");
  }
}
