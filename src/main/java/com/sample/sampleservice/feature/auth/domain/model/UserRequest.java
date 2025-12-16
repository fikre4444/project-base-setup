package com.sample.sampleservice.feature.auth.domain.model;

import com.sample.sampleservice.feature.auth.infrastructure.secondary.model.Credential;
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
public class UserRequest {

    private String username;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    @Builder.Default
    private Boolean enabled = true;

    @Builder.Default
    private Boolean emailVerified = true;

    @Builder.Default
    private List<Credential> credentials = new ArrayList<>();

    @Builder.Default
    private Map<String, Object> attributes = new HashMap<>();

    public void clearCredentials() {
        this.credentials = List.of();
    }
}
