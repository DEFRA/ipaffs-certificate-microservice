package uk.gov.defra.tracesx.integration.certificate.helpers;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.defra.tracesx.integration.certificate.Properties;
import uk.gov.defra.tracesx.integration.certificate.entity.Certificate;
import org.apache.commons.lang3.StringUtils;


public class CertificateServiceHelper {

  public static final String X_AUTH_BASIC = "x-auth-basic";
  private final String REFERENCE = "CVEDA.GB.2018.1234567";

  public CertificateServiceHelper() {
    assertNotNullOrEmpty(Properties.SERVICE_USERNAME, "Username is empty");
    assertNotNullOrEmpty("Password is empty", Properties.SERVICE_PASSWORD);
    assertNotNullOrEmpty("Url is empty", Properties.SERVICE_BASE_URL);
  }

  private static void assertNotNullOrEmpty(String value, String message) {
    if (StringUtils.isBlank(value)) {
      throw new NullPointerException(message);
    }
  }

  public String getAsJsonString(Certificate certificate) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      return objectMapper.writeValueAsString(certificate);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Can't parse certificate");
    }
  }

  public String getUrl(String path) {
    return Properties.SERVICE_BASE_URL + path;
  }

  public String createUrl() {
    return getUrl("/certificate/" + REFERENCE + "?url=http://ins.com/certificate/001");
  }
}
