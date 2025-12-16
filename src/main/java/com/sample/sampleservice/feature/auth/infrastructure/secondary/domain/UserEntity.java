package com.sample.sampleservice.feature.auth.infrastructure.secondary.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import com.sample.sampleservice.shared.audit.secondary.AbstractAuditingEntity;

@Entity
@Table(name = "t_s_users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity extends AbstractAuditingEntity<String>{

    @Id
    private String id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "password_hash", length = 60, nullable = false)
    private String password;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "created_timestamp", nullable = false)
    private long createdTimestamp;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
        name = "t_user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_name")
    )
    @Builder.Default
    private Set<RoleEntity> roles = new HashSet<>();


    @PrePersist
    public void ensureId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }
}
