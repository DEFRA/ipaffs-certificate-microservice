package uk.gov.defra.tracesx.certificate.security.jwks;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import java.security.Key;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.defra.tracesx.certificate.exceptions.InsSecurityException;

@Component
public class JwksCache {

  private static final Logger LOGGER = LoggerFactory.getLogger(JwksCache.class);

  private final List<ClaimsAwareJwkProvider> allJwkProviders;
  private final Map<String, ClaimsAwareJwkProvider> jwkProviderMap;

  public JwksCache(
      @Qualifier("jwksConfiguration") List<JwksConfiguration> jwksConfiguration,
      JwkProviderFactory jwkProviderFactory) {
    allJwkProviders =
        Collections.unmodifiableList(
            jwksConfiguration
                .stream()
                .map(jwkProviderFactory::newInstance)
                .collect(Collectors.toList()));
    jwkProviderMap = new HashMap<>();
  }

  public KeyAndClaims getPublicKey(String kid) {
    try {
      ClaimsAwareJwkProvider jwkProvider = getJwkFromProvider(kid);
      Jwk jwk = jwkProvider.get(kid);
      return KeyAndClaims.builder()
          .aud(jwkProvider.getAudience())
          .iss(jwkProvider.getIssuer())
          .key(jwk.getPublicKey())
          .build();
    } catch (JwkException e) {
      LOGGER.error("Unable to get a public signing certificate for the id token", e);
      throw new InsSecurityException("Invalid security configuration");
    }
  }

  private ClaimsAwareJwkProvider getJwkFromProvider(String kid) {
    if (jwkProviderMap.containsKey(kid)) {
      return jwkProviderMap.get(kid);
    } else {
      return scanProviders(kid);
    }
  }

  private ClaimsAwareJwkProvider scanProviders(String kid) {
    for (ClaimsAwareJwkProvider jwkProvider : allJwkProviders) {
      try {
        jwkProvider.get(kid);
        jwkProviderMap.put(kid, jwkProvider);
        return jwkProvider;
      } catch (JwkException e) {
        LOGGER.debug("Provider {} does not contain key {}", jwkProvider.getIssuer(), kid);
        LOGGER.debug("JwkProvider throw exception", e);
      }
    }
    LOGGER.error("Unable to find any provider for key {}", kid);
    throw new InsSecurityException("Invalid security configuration");
  }

  @Data
  @Builder
  @Getter
  @EqualsAndHashCode
  public static class KeyAndClaims {
    private String iss;
    private String aud;
    private Key key;
  }
}
