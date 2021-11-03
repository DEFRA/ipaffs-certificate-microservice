package uk.gov.defra.tracesx.certificate.integration;

import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.runner.RunWith;
import uk.gov.defra.tracesx.common.security.tests.AbstractApiAuthenticationTest;
import uk.gov.defra.tracesx.common.security.tests.ApiMethod;
import uk.gov.defra.tracesx.integration.certificate.helpers.CertificateServiceHelper;


@RunWith(Theories.class)
public class TestApiAuthentication extends AbstractApiAuthenticationTest {

  private static final CertificateServiceHelper helper = new CertificateServiceHelper();

  @DataPoints("API Methods")
  public static ApiMethod[] getApiMethods() {
    return new ApiMethod[]{
        spec -> spec.get(helper.createUrl())
    };
  }
}
