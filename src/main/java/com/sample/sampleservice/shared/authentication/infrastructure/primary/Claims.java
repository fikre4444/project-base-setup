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

    static final String CLAIMS_NAMESPACE = "http://localhost:8180";

    static List<GrantedAuthority> extractAuthorityFromClaims(Map<String, Object> claims) {
        return mapRolesToGrantedAuthorities(getRolesFromClaims(claims));
    }

    @SuppressWarnings("unchecked")
    private static Collection<String> getRolesFromClaims(Map<String, Object> claims) {

        return ((Map<String, Object>) claims.getOrDefault("realm_access", new HashMap<>()))
                .values()
                .stream()
                .map(a -> (List<String>) a)
                .reduce(new ArrayList<>(), (first, second) -> {
                    first.addAll(second);
                    return first;
                });
    }

    @SuppressWarnings("java:S6204")
    private static List<GrantedAuthority> mapRolesToGrantedAuthorities(Collection<String> roles) {
        log.info("claim info {}", roles);
        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }
}
