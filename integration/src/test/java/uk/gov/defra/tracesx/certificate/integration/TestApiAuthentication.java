package uk.gov.defra.tracesx.certificate.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.defra.tracesx.common.security.tests.AbstractApiAuthenticationTest;
import uk.gov.defra.tracesx.common.security.tests.ApiMethod;
import uk.gov.defra.tracesx.integration.certificate.helpers.CertificateServiceHelper;

public class TestApiAuthentication extends AbstractApiAuthenticationTest {
  private static final CertificateServiceHelper helper = new CertificateServiceHelper();

  @ParameterizedTest()
  @MethodSource("apiMethods")
  void testApiMethods(){
    assertNotNull(apiMethods());
  }

  public static ApiMethod[] apiMethods() {
    return new ApiMethod[]{
        spec -> spec.get(helper.createUrl())
    };
  }
}
