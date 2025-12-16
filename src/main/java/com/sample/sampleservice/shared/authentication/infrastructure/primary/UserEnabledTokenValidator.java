package com.sample.sampleservice.shared.authentication.infrastructure.primary;

import com.sample.sampleservice.feature.auth.infrastructure.secondary.domain.UserEntity;
import com.sample.sampleservice.feature.auth.infrastructure.secondary.repository.UserEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEnabledTokenValidator implements OAuth2TokenValidator<Jwt> {

    private final UserEntityRepository userEntityRepository;

    private static final OAuth2Error USER_DISABLED = new OAuth2Error(
            "user_disabled",
            "The user account is disabled or does not exist.",
            null
    );

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        String userId = jwt.getClaimAsString("uid"); 
        
        if (userId == null) {
             userId = jwt.getSubject(); 
        }

        Optional<UserEntity> userOpt = userEntityRepository.findById(userId);

        if (userOpt.isEmpty()) {
            log.warn("Token validation failed: User ID {} not found in DB.", userId);
            return OAuth2TokenValidatorResult.failure(USER_DISABLED);
        }

        UserEntity user = userOpt.get();
        if (!user.isEnabled()) {
            log.warn("Token validation failed: User {} is disabled.", user.getUsername());
            return OAuth2TokenValidatorResult.failure(USER_DISABLED);
        }

        return OAuth2TokenValidatorResult.success();
    }
}
