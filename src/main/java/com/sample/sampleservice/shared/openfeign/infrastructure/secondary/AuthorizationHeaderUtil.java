package com.sample.sampleservice.shared.openfeign.infrastructure.secondary;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthorizationHeaderUtil {

    public Optional<String> getAuthorizationHeader() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            Jwt jwt = jwtToken.getToken();
            String tokenValue = jwt.getTokenValue();
            
            return Optional.of("Bearer " + tokenValue);
        }
        
        return Optional.empty();
    }

}
