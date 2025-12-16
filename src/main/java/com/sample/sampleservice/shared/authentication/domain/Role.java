package com.sample.sampleservice.shared.authentication.domain;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sample.sampleservice.shared.error.domain.Assert;

@Slf4j
public enum Role {

    USER,
    ANONYMOUS,
    ADMIN,
    SYSTEM,
    UNKNOWN;

    private static final Map<String, Role> ROLES = buildRoles();

    private static Map<String, Role> buildRoles() {
        return Stream.of(values()).collect(Collectors.toUnmodifiableMap(Role::key, Function.identity()));
    }

    public static Role from(String role) {
        Assert.notBlank("role", role);

        return ROLES.getOrDefault(role, UNKNOWN);
    }

    public String key() {
        return "ROLE_%s".formatted(name());
    }

    public String roleName() {
        return "ROLE_%s".formatted(name().toUpperCase());
    }
}
