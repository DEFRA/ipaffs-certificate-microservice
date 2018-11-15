package uk.gov.defra.tracesx.certificate;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.Before;
import org.junit.Test;

public class CertificateConfigurationTest {

  private CertificateConfiguration certificateConfiguration;

  @Before
  public void setUp() {
    certificateConfiguration = new CertificateConfiguration();
  }

  @Test
  public void testBeanIsLoaded() {
    assertNotNull(certificateConfiguration.getBlobStorage());
  }
}
