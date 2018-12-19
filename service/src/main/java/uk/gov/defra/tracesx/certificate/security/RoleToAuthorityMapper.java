package uk.gov.defra.tracesx.certificate.security;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class RoleToAuthorityMapper {

  private static final int ORG_INDEX = 0;
  private static final int ROLE_INDEX = 1;
  private static final int STATUS_INDEX = 2;

  public List<GrantedAuthority> mapRoles(List<String> roles) {
    return roles.stream().map(this::mapRole).collect(toList());
  }

  private GrantedAuthority mapRole(String role) {
    String parts[] = role.split(":");
    if (parts.length == 3) {
      return OrganisationGrantedAuthority.builder()
          .organisation(parts[ORG_INDEX])
          .authority(parts[ROLE_INDEX])
          .status(parts[STATUS_INDEX])
          .build();
    }
    return new SimpleGrantedAuthority(role);
  }
}
