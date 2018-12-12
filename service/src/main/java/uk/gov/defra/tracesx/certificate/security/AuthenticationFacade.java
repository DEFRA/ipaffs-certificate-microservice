package uk.gov.defra.tracesx.certificate.security;

import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFacade {

     Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

     void replaceAuthorities(List<SimpleGrantedAuthority> permissions) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        getAuthentication().getPrincipal(),
                        getAuthentication().getCredentials(),
                        permissions));
    }
}
