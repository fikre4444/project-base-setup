package com.sample.sampleservice.shared.audit.secondary;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import com.sample.sampleservice.shared.authentication.application.AuthenticatedUser;
import com.sample.sampleservice.shared.authentication.domain.Role;
import com.sample.sampleservice.shared.authentication.domain.Username;

import java.util.Optional;

@Component
public class SpringSecurityAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {

        try {
            return Optional.of(AuthenticatedUser.optionalUsername()
                    .map(Username::username)
                    .orElse(Role.SYSTEM.name()));
        } catch (Throwable ex) {
            return Optional.of(Role.ANONYMOUS.name());
        }
    }
}
