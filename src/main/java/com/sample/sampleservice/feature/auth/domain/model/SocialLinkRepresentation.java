package com.sample.sampleservice.feature.auth.domain.model;

public record SocialLinkRepresentation(
    String socialProvider,
    String socialUserId,
    String socialUsername
) {}
