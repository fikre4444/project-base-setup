package com.sample.sampleservice.feature.auth.infrastructure.secondary.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDto {

    private String username;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    @Builder.Default
    private Boolean enabled = true;

    @Builder.Default
    private Boolean emailVerified = false;

    @Builder.Default
    private List<Credential> credentials = new ArrayList<>();

    @Builder.Default
    private Map<String, Object> attributes = new HashMap<>();

    private List<String> realmRoles;
}
