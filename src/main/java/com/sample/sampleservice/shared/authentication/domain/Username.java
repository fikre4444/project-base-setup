package com.sample.sampleservice.shared.authentication.domain;

import org.apache.commons.lang3.StringUtils;

import com.sample.sampleservice.shared.error.domain.Assert;

import java.util.Optional;

public record Username(String username) {
    public Username {
        Assert.field("username", username).notBlank().maxLength(100);
    }

    public static Optional<Username> of(String username) {
        return Optional.ofNullable(username).filter(StringUtils::isNotBlank).map(Username::new);
    }

    public String get() {
        return username();
    }
}
