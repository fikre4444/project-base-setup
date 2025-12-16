package com.sample.sampleservice.feature.auth.domain.model;

import java.util.Map;

public record UserProfileAttributeMetadata(
    String name,
    String displayName,
    boolean required,
    boolean readOnly,
    Map<String, String> annotations,
    Map<String, Map<String, String>> validators,
    String group,
    boolean multivalued
) {}
