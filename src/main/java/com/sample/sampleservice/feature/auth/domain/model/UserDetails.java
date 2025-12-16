package com.sample.sampleservice.feature.auth.domain.model;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetails implements Serializable {

    @Serial
    private static final long serialVersionUID = -1608009883525411542L;

    private String id;
    private String username;
    private String email;
    
    private String firstName;
    private String lastName;
    private String phoneNumber;
    
    private boolean enabled;
    private boolean emailVerified;
    private long createdTimestamp;

    @Builder.Default
    private List<String> roles = new ArrayList<>();

    @Builder.Default
    private List<String> requiredActions = new ArrayList<>(); // e.g. ["VERIFY_EMAIL"]

    public enum RequiredAction {
        UPDATE_PASSWORD,
        VERIFY_EMAIL
    }
    
    public void addRole(String role) {
        if (this.roles == null) {
            this.roles = new ArrayList<>();
        }
        this.roles.add(role);
    }
}