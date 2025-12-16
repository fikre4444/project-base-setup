package com.sample.sampleservice.shared.authentication.domain;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

import com.sample.sampleservice.shared.error.domain.Assert;

public record Roles(Set<Role> roles) {

    public static final Roles EMPTY = new Roles(null);

    public Roles(Set<Role> roles) {
        this.roles = roles;
    }

    public boolean hasRole() {
        return !roles.isEmpty();
    }

    public boolean hasRole(Role role) {
        Assert.notNull("role", role);

        return roles.contains(role);
    }

    public boolean hasRole(Role... roles) {
        Assert.notNull("role", roles);

        return Arrays.stream(roles).anyMatch(this.roles::contains);
    }

    public Stream<Role> stream() {
        return get().stream();
    }

    public Set<Role> get() {
        return roles();
    }
}
