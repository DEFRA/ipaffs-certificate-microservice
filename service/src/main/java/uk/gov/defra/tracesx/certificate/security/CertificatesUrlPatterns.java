package uk.gov.defra.tracesx.certificate.security;

import static java.util.Arrays.asList;

import org.springframework.stereotype.Component;
import uk.gov.defra.tracesx.common.security.ServiceUrlPatterns;

import java.util.List;

@Component
public class CertificatesUrlPatterns implements ServiceUrlPatterns {

  @Override
  public List<String> getAuthorizedPatterns() {
    return asList("/certificate/**", "/certificate/*", "certificate");
  }
}
