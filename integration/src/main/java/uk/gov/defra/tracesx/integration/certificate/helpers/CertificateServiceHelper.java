package uk.gov.defra.tracesx.integration.certificate.helpers;

import org.apache.commons.lang3.StringUtils;
import uk.gov.defra.tracesx.integration.certificate.Properties;

public class CertificateServiceHelper {

  private final String REFERENCE = "CVEDA.GB.2018.1234567";

  public CertificateServiceHelper() {
    assertNotNullOrEmpty("Url is empty", Properties.SERVICE_BASE_URL);
  }

  private static void assertNotNullOrEmpty(String value, String message) {
    if (StringUtils.isBlank(value)) {
      throw new NullPointerException(message);
    }
  }

  public String getUrl(String path) {
    return Properties.SERVICE_BASE_URL + path;
  }

  public String createUrl() {
    return getUrl("/certificate/" + REFERENCE + "?url=http://somewebsite.com/certificate/001");
  }
}
