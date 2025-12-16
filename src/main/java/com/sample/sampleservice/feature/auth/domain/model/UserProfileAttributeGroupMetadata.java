package com.sample.sampleservice.feature.auth.domain.model;

import java.util.Map;

public record UserProfileAttributeGroupMetadata(
    String name,
    String displayHeader,
    String displayDescription,
    Map<String, String> annotations
) {}
