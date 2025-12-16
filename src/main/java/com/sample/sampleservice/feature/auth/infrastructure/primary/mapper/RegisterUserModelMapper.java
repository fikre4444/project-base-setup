package com.sample.sampleservice.feature.auth.infrastructure.primary.mapper;

import com.sample.sampleservice.feature.auth.api.rest.v1.model.RegisterUserRequest;
import com.sample.sampleservice.feature.auth.domain.model.CreateUser;
import com.sample.sampleservice.shared.mapper.infrastructure.primary.ModelMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RegisterUserModelMapper extends ModelMapper<RegisterUserRequest, CreateUser> {
}
