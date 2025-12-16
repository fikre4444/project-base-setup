// In: com.sample.sampleservice.feature.auth.infrastructure.secondary.entity
package com.sample.sampleservice.feature.auth.infrastructure.secondary.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "t_s_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleEntity {

    @Id
    @Column(name = "name")
    private String name;
    
}