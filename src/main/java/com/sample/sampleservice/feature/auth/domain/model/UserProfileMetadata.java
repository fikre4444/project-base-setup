package com.sample.sampleservice.feature.auth.domain.model;

import java.util.List;

public record UserProfileMetadata(
    List<UserProfileAttributeMetadata> attributes,
    List<UserProfileAttributeGroupMetadata> groups
) {}
