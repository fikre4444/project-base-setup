package com.sample.sampleservice.feature.auth.domain.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class UserLogin implements Serializable {

    @Serial
    private static final long serialVersionUID = -9043510191351261148L;

    @NotBlank
    @ToString.Include
    private String username;

    @NotBlank
    @SuppressWarnings("name")
    private String password;

    @NotBlank
    @SuppressWarnings("name")
    private String grant_type;

    @NotBlank
    @SuppressWarnings("name")
    private String client_id;

    @NotBlank
    @SuppressWarnings("name")
    private String client_secret;

    @NotBlank
    @SuppressWarnings("name")
    private String refresh_token;

}
