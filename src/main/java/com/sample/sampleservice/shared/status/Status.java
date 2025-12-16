package com.sample.sampleservice.shared.status;

import java.util.Arrays;
import java.util.Set;

public enum Status {

    PENDING {
        @Override
        public Set<Status> allowedTransition() { return Set.of(DELETED, ACTIVE); }
    },
    ACTIVE {
        @Override
        public Set<Status> allowedTransition() { return Set.of(DELETED, INACTIVE, ASSIGNED); }
    },
    INACTIVE {
        @Override
        public Set<Status> allowedTransition() { return Set.of(DELETED, ACTIVE, INACTIVE); }
    },
    ASSIGNED {
        @Override
        public Set<Status> allowedTransition() { return Set.of(ACTIVE); }
    },
    DELETED {
        @Override
        public Set<Status> allowedTransition() { return Set.of(INACTIVE); }
    };

    public abstract Set<Status> allowedTransition();

    public static Status fromString(String value) {
        return Arrays.stream(values())
            .filter(status -> status.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }

    public boolean is(Status... status) {
        return Arrays.stream(status).anyMatch(st -> st == this);
    }

    public boolean isNot(Status... status) {
        return !is(status);
    }

    public boolean isNot(Status status) {
        return this != status;
    }

    public boolean is(String status) {
        return this == Status.fromString(status);
    }

    public boolean isNot(String status) {
        return this != Status.fromString(status);
    }

    public boolean can(Status status) {
        return this.allowedTransition().stream().anyMatch(st -> st == status);
    }
}
