package com.sample.sampleservice.feature.auth.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class OAuth2TokenResult implements Serializable {

    @ToString.Include
    @JsonProperty("token_type")
    private String tokenType;

    @ToString.Include
    private String scope;

    @ToString.Include
    @JsonProperty("expires_in")
    private Long expiresIn;

    @ToString.Include
    @JsonProperty("ext_expires_in")
    private Long extExpiresIn;

    @ToString.Include
    @JsonProperty("expires_on")
    private Long expiresOn;

    @JsonProperty("not-before-policy")
    private Long notBefore;

    private UUID resource;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("id_token")
    private String idToken;

    @JsonProperty("session_state")
    private String sessionState;

    @JsonProperty("refresh_expires_in")
    private String refreshExpiresIn;
}
