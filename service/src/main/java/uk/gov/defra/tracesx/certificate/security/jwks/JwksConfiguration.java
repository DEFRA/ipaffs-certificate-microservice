package uk.gov.defra.tracesx.certificate.security.jwks;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.net.URL;

@Data
@Getter
@Builder
@EqualsAndHashCode
public class JwksConfiguration {
  private URL jwksUrl;
  private String audience;
  private String issuer;
}
