package com.sample.sampleservice.feature.auth.domain.model;

public record FederatedIdentityRepresentation(
    String identityProvider,
    String userId,
    String userName
) {}
