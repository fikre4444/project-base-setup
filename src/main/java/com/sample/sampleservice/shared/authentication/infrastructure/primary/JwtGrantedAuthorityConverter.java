package com.sample.sampleservice.shared.authentication.infrastructure.primary;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Slf4j
@Component
class JwtGrantedAuthorityConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    public JwtGrantedAuthorityConverter() {
        // Bean extracting authority.
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        return Claims.extractAuthorityFromClaims(jwt.getClaims());
    }
}
