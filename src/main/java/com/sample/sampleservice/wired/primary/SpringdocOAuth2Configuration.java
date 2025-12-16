package com.sample.sampleservice.wired.primary;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@AutoConfigureBefore(SpringdocConfiguration.class)
class SpringdocOAuth2Configuration {

    @Value("${springdoc.oauth2.authorization-url}")
    private String authorizationUrl;

    @Bean
    GlobalOpenApiCustomizer oauthOpenApi() {
        return openApi ->
                openApi
                        .components(oauthComponents(openApi.getComponents()))
                        .addSecurityItem(new SecurityRequirement().addList("security_auth", List.of("insurance")));
    }

    private Components oauthComponents(Components existingComponents) {
        return existingComponents.addSecuritySchemes(
                "security_auth",
                new SecurityScheme()
                        .type(SecurityScheme.Type.OAUTH2)
                        .flows(new OAuthFlows().implicit(new OAuthFlow().authorizationUrl(authorizationUrl)))
        );
    }
}
