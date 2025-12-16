package com.sample.sampleservice.shared.openfeign.infrastructure.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.UUID;

@Slf4j
public record OAuthIdpTokenResponse(@JsonProperty("token_type") String tokenType, String scope,
                                    @JsonProperty("expires_in") Long expiresIn,
                                    @JsonProperty("ext_expires_in") Long extExpiresIn,
                                    @JsonProperty("expires_on") Long expiresOn,
                                    @JsonProperty("not-before-policy") Long notBefore, UUID resource,
                                    @JsonProperty("access_token") String accessToken,
                                    @JsonProperty("refresh_token") String refreshToken,
                                    @JsonProperty("id_token") String idToken,
                                    @JsonProperty("session_state") String sessionState,
                                    @JsonProperty("refresh_expires_in") String refreshExpiresIn) implements Serializable {
}
