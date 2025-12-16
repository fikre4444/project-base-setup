package com.sample.sampleservice.feature.auth.infrastructure.secondary.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Validated
@Data
@Configuration
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakClientConfig {

    @NotBlank
    private String realm;

    @Valid
    @NotNull
    private ClientConfig client;

    @Data
    public static class ClientConfig {

        @NotBlank
        private String id;

        @NotBlank
        private String secret;
    }
}
