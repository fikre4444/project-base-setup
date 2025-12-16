package com.sample.sampleservice.shared.authentication.infrastructure.primary;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor
final class Claims {

    static List<GrantedAuthority> extractAuthorityFromClaims(Map<String, Object> claims) {
        return mapRolesToGrantedAuthorities(getRolesFromClaims(claims));
    }

    @SuppressWarnings("unchecked")
    private static Collection<String> getRolesFromClaims(Map<String, Object> claims) {
        return claims.containsKey("roles") ? ((List<String>)claims.get("roles")) : Collections.emptyList();
    }

    @SuppressWarnings("java:S6204")
    private static List<GrantedAuthority> mapRolesToGrantedAuthorities(Collection<String> roles) {
        log.info("claim info {}", roles);
        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }
}
