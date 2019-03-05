package uk.gov.defra.tracesx.certificate.integration;

import uk.gov.defra.tracesx.common.security.tests.AbstractAdminAuthenticationTest;
import uk.gov.defra.tracesx.integration.certificate.helpers.CertificateServiceHelper;


public class TestAdminAuthentication extends AbstractAdminAuthenticationTest {

  private static final CertificateServiceHelper helper = new CertificateServiceHelper();

  @Override
  protected String getAdminUrl() {
    return helper.getUrl("/admin");
  }

  @Override
  protected String getRootUrl() {
    return helper.getUrl("/");
  }
}
