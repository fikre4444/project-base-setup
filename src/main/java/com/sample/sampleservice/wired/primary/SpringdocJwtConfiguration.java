package com.sample.sampleservice.wired.primary;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureBefore(SpringdocConfiguration.class)
class SpringdocJwtConfiguration { // Renamed from OAuth2Configuration

    // We no longer need authorizationUrl since we aren't redirecting

    @Bean
    GlobalOpenApiCustomizer jwtOpenApi() {
        return openApi -> {
            // 1. Ensure Components exist
            Components components = openApi.getComponents();
            if (components == null) {
                components = new Components();
                openApi.setComponents(components);
            }

            // 2. Add the Bearer Token Security Scheme
            components.addSecuritySchemes(
                    "bearer-jwt", // ID for this scheme
                    new SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("Enter your JWT token here")
            );

            // 3. Apply it globally to all endpoints
            openApi.addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
        };
    }
}