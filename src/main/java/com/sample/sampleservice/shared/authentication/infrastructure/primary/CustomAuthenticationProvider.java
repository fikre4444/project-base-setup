package com.sample.sampleservice.shared.authentication.infrastructure.primary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.nimbusds.jwt.JWTParser;
import com.sample.sampleservice.shared.authentication.application.JwtTokenExpiredException;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final JwtDecoder jwtDecoder;
    private Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter = new JwtAuthenticationConverter();
    private final HttpServletRequest request; 

    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        BearerTokenAuthenticationToken bearer = (BearerTokenAuthenticationToken) authentication;
        Jwt jwt = this.getJwt(bearer);
        AbstractAuthenticationToken token = this.jwtAuthenticationConverter.convert(jwt);
        com.sample.sampleservice.shared.error.domain.Assert.notNull("JWT token", token);
        if (token.getDetails() == null) {
            token.setDetails(bearer.getDetails());
        }

        log.debug("Authenticated token");
        return token;
    }

    private Jwt getJwt(BearerTokenAuthenticationToken bearer) {
        try {
            return this.jwtDecoder.decode(bearer.getToken());
        } catch (BadJwtException badJwt) {
			BadJwtException failed = badJwt;
            try {
                //this checks if the failure is due to token expiry and if so it throws a jwt expired exception
                com.nimbusds.jwt.JWT parsedJwt = JWTParser.parse(bearer.getToken());
                java.util.Date expirationTime = parsedJwt.getJWTClaimsSet().getExpirationTime();
                if (expirationTime != null && expirationTime.before(new java.util.Date())) {
                    String requestUri = request.getRequestURI();
                    if (requestUri != null && requestUri.contains("/auth/refresh")) {
                        log.warn("Expired token received for refresh endpoint. Allowing request to proceed for token exchange.");
                        return Jwt.withTokenValue(bearer.getToken()).header("issuer", "sample").claim("api_key", bearer.getToken()).build();
                    } else {
                        log.warn("Authentication failed: The provided JWT has expired.");
                        throw new JwtTokenExpiredException("Token has expired"); 
                    }
                }
            } catch (java.text.ParseException e) {
                log.warn("Token could not be parsed as a JWT, proceeding with API key logic.");
            }
            log.debug("Failed to authenticate since the JWT was invalid {}", badJwt.getMessage());
            // return Jwt.withTokenValue(bearer.getToken()).header("issuer", "sample").claim("api_key", bearer.getToken()).build();
			throw new InvalidBearerTokenException(failed.getMessage(), failed);
        } catch (JwtException jwtEx) {
            throw new AuthenticationServiceException(jwtEx.getMessage(), jwtEx);
        }
    }

    public boolean supports(Class<?> authentication) {
        return BearerTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }

    public void setJwtAuthenticationConverter(Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter) {
        Assert.notNull(jwtAuthenticationConverter, "jwtAuthenticationConverter cannot be null");
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
    }
}
