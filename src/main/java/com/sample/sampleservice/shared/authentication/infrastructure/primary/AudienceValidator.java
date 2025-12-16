package com.sample.sampleservice.shared.authentication.infrastructure.primary;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import com.sample.sampleservice.shared.error.domain.Assert;

import java.util.List;

@Slf4j
class AudienceValidator implements OAuth2TokenValidator<Jwt> {

    private final OAuth2Error error = new OAuth2Error("invalid_token", "The required audience is missing", null);

    private final List<String> allowedAudience;

    public AudienceValidator(List<String> allowedAudience) {
//    Assert.notEmpty(allowedAudience, "Allowed audience should not be null or empty.");
        this.allowedAudience = allowedAudience;
    }

    public OAuth2TokenValidatorResult validate(Jwt jwt) {
//        List<String> audience = jwt.getAudience();
//        Assert.notNull("Audience", audience);

//        if (audience.stream().anyMatch(allowedAudience::contains)) {
            return OAuth2TokenValidatorResult.success();
//        }

//        log.warn("Invalid audience: {}", audience);
//        return OAuth2TokenValidatorResult.failure(error);
    }
}
