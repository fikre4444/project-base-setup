package com.sample.sampleservice.feature.auth.infrastructure.secondary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sample.sampleservice.feature.auth.infrastructure.secondary.domain.RoleEntity;

@Repository
public interface RoleEntityRepository extends JpaRepository<RoleEntity, String> {
}