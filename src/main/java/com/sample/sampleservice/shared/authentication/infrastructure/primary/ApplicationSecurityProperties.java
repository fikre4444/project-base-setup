package com.sample.sampleservice.shared.authentication.infrastructure.primary;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "application.security", ignoreUnknownFields = false)
class ApplicationSecurityProperties {

    private static final String CONTENT_SECURITY_POLICY =
            """
                    default-src 'self'; frame-src 'self' data:; \
                    script-src 'self' 'unsafe-inline' 'unsafe-eval' https://storage.googleapis.com; \
                    style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; \
                    img-src 'self' data:; \
                    font-src 'self' data: https://fonts.gstatic.com;\
                    """;

    private final OAuth2 oauth2 = new OAuth2();

    private String contentSecurityPolicy = CONTENT_SECURITY_POLICY;

    public static class OAuth2 {

        private final List<String> audience = new ArrayList<>();

        public List<String> getAudience() {
            audience.add("http://localhost:8180/realms/sample");
            return Collections.unmodifiableList(audience);
        }

        public void setAudience(@NotNull List<String> audience) {
            this.audience.addAll(audience);
        }
    }
}
