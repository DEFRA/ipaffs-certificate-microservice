package uk.gov.defra.tracesx.certificate.security.jwt;

import static uk.gov.defra.tracesx.certificate.security.jwt.JwtContants.ROLES;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import uk.gov.defra.tracesx.certificate.exceptions.InsSecurityException;
import uk.gov.defra.tracesx.certificate.security.IdTokenUserDetails;
import uk.gov.defra.tracesx.certificate.security.RoleToAuthorityMapper;

@Component
public class JwtUserMapper {

  private static final Logger LOGGER = LoggerFactory.getLogger(JwtUserMapper.class);
  public static final String NAME = "name";
  public static final String UPN = "upn";
  public static final String OID = "oid";

  private final RoleToAuthorityMapper roleToAuthorityMapper;

  @Autowired
  public JwtUserMapper(RoleToAuthorityMapper roleToAuthorityMapper) {
    this.roleToAuthorityMapper = roleToAuthorityMapper;
  }

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
      throw missingRequiredClaims();
    }
    return value;
  }

  private List<GrantedAuthority> getAuthorities(Map<String, Object> body) {
    if (!body.containsKey("roles")) {
      LOGGER.error("The JWT token is missing the claim 'roles'");
      throw missingRequiredClaims();
    }

    Object rolesObj = body.get(ROLES);
    if (!(rolesObj instanceof List)) {
      LOGGER.error("The JWT token does not contain a list of 'roles'");
      throw missingRequiredClaims();
    }

    List<String> roles = (List) rolesObj;
    return roleToAuthorityMapper.mapRoles(roles);
  }

  private InsSecurityException missingRequiredClaims() {
    return new InsSecurityException("User is missing required claims");
  }
}
