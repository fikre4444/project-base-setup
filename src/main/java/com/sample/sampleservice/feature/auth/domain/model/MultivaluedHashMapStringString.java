package com.sample.sampleservice.feature.auth.domain.model;

import java.util.Map;
import java.util.List;

public record MultivaluedHashMapStringString(
    Map<String, List<String>> additionalProperties
) {}
