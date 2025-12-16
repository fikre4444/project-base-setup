package com.sample.sampleservice.shared.authentication.infrastructure.primary;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Configuration
class ApplicationSecurityProperties {

    private static final String CONTENT_SECURITY_POLICY =
            """
                    default-src 'self'; frame-src 'self' data:; \
                    script-src 'self' 'unsafe-inline' 'unsafe-eval' https://storage.googleapis.com; \
                    style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; \
                    img-src 'self' data:; \
                    font-src 'self' data: https://fonts.gstatic.com;\
                    """;

    private String contentSecurityPolicy = CONTENT_SECURITY_POLICY;

}
