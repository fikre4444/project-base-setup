package com.sample.sampleservice.shared.authentication.domain;

public record User(String id, String firstName, String lastName, String username, String email, String langKey,
                   String imageUrl, boolean active) {
}
