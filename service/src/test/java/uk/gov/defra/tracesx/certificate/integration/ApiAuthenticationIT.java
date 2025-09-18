package uk.gov.defra.tracesx.certificate.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.defra.tracesx.common.security.tests.ApiMethod;

class ApiAuthenticationIT extends IntegrationBase {

  @ParameterizedTest()
  @MethodSource("apiMethods")
  void testApiMethods(){
    assertNotNull(apiMethods());
  }

  public static ApiMethod[] apiMethods() {
    return new ApiMethod[]{
        spec -> spec.get(API.createUrl())
    };
  }

}
