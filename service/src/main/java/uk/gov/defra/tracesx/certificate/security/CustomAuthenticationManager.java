package uk.gov.defra.tracesx.certificate.security;

import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationManager implements AuthenticationManager {

    @Value("${spring.security.user.name}")
    private String username;

    @Value("${spring.security.user.password}")
    private String password;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        final Optional<Object> username = Optional.ofNullable(authentication.getPrincipal());
        final Optional<Object> password = Optional.ofNullable(authentication.getCredentials());

        if(isValidate(username,password)){
            return new UsernamePasswordAuthenticationToken(
                    username.get(),
                    password.get(),
                    Collections.emptyList());
        }
        throw new RuntimeException(
                "Failed to perform Basic authentication due to  missing user/password");
    }

    private boolean isValidate(final Optional<Object> username, final Optional<Object> password){
        return username.isPresent()
                && password.isPresent()
                && username.get().toString().equals(this.username)
                && password.get().toString().equals(this.password);
    }
}
