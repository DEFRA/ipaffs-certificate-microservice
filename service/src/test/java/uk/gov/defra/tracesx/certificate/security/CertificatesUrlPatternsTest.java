package uk.gov.defra.tracesx.certificate.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;


class CertificatesUrlPatternsTest {

  @Test
  void getAuthorizedPatterns_ReturnsAllowedCertificateUrlPatterns() {
    CertificatesUrlPatterns certificatesUrlPatterns = new CertificatesUrlPatterns();
    assertThat(certificatesUrlPatterns.getAuthorizedPatterns()).containsExactly("/certificate/**",
        "/certificate/*", "certificate");
  }
}