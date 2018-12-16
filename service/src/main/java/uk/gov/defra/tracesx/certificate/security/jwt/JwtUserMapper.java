package uk.gov.defra.tracesx.certificate.security.jwt;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import uk.gov.defra.tracesx.certificate.exceptions.InsSecurityException;
import uk.gov.defra.tracesx.certificate.security.IdTokenUserDetails;

@Component
public class JwtUserMapper {

  private static final Logger LOGGER = LoggerFactory.getLogger(JwtUserMapper.class);
  public static final String NAME = "name";
  public static final String UPN = "upn";
  public static final String OID = "oid";

  public IdTokenUserDetails createUser(Map<String, Object> decoded, String idToken) {
    return IdTokenUserDetails.builder()
        .idToken(idToken)
        .displayName(getRequiredClaim(NAME, decoded))
        .username(getRequiredClaim(UPN, decoded))
        .userObjectId(getRequiredClaim(OID, decoded))
        .authorities(getAuthorities(decoded))
        .build();
  }

  private String getRequiredClaim(String claimName, Map<String, Object> body) {
    String value = (String) body.get(claimName);
    if (StringUtils.isEmpty(value)) {
      LOGGER.error("The JWT token is missing the claim '{}'", claimName);
      throw new InsSecurityException("User is missing required claims");
    }
    return value;
  }

  private List<SimpleGrantedAuthority> getAuthorities(Map<String, Object> body) {
    if (!body.containsKey("roles")) {
      LOGGER.error("The JWT token is missing the claim 'roles'");
      throw new InsSecurityException("User is missing required claims");
    }
    List<String> roles = (List) body.get("roles");
    return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
  }
}
