package com.sample.sampleservice.feature.auth.domain.model;

import java.util.List;

public record UserConsentRepresentation(
    String clientId,
    List<String> grantedClientScopes,
    long createdDate,
    long lastUpdatedDate,
    List<String> grantedRealmRoles
) {}
