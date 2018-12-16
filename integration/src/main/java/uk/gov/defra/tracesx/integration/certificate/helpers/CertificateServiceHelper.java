package uk.gov.defra.tracesx.integration.certificate.helpers;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import uk.gov.defra.tracesx.integration.certificate.Properties;
import uk.gov.defra.tracesx.integration.certificate.entity.Certificate;

public class CertificateServiceHelper {

  public static final String X_AUTH_BASIC = "x-auth-basic";
  private final String REFERENCE = "CVEDA.GB.2018.1234567";

  private String basicAuthHeader;

  public CertificateServiceHelper() {
    assertNotNull("User name is empty", Properties.SERVICE_USERNAME);
    assertNotNull("Password is empty", Properties.SERVICE_PASSWORD);
    assertNotNull("Url is empty", Properties.SERVICE_BASE_URL);
    String encodedBasicAuth =
        Base64.getEncoder()
            .encodeToString(
                (Properties.SERVICE_USERNAME + ":" + Properties.SERVICE_PASSWORD)
                    .getBytes(StandardCharsets.UTF_8));
    basicAuthHeader = "Basic " + encodedBasicAuth;
  }

  public String getAsJsonString(Certificate certificate) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      return objectMapper.writeValueAsString(certificate);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Can't parse certificate");
    }
  }

  public String getBasicAuthHeader() {
    return basicAuthHeader;
  }

  public String getEncodedInvalidBasicAuth() {
    String authString = Properties.SERVICE_USERNAME + ":" + Properties.SERVICE_PASSWORD + "INVALID";
    byte[] authStringBytes = authString.getBytes(StandardCharsets.UTF_8);
    String encoded = Base64.getEncoder().encodeToString(authStringBytes);
    return "Basic " + encoded;
  }

  public String getUrl(String path) {
    return Properties.SERVICE_BASE_URL + path;
  }

  public String createUrl() {
    return getUrl("/certificate/" + REFERENCE + "?url=http://ins.com/certificate/001");
  }
}
