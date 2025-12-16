package com.sample.sampleservice.feature.auth.infrastructure.secondary.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Credential {

    @NotNull
    private Boolean temporary;

    @NotBlank
    private String value;

    @NotBlank
    @Builder.Default
    private String type = "password";
}
