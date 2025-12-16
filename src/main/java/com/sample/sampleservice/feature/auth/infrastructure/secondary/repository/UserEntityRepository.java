package com.sample.sampleservice.feature.auth.infrastructure.secondary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.sample.sampleservice.feature.auth.infrastructure.secondary.domain.UserEntity;

@Repository
public interface UserEntityRepository extends JpaRepository<UserEntity, String>, JpaSpecificationExecutor<UserEntity> {
}