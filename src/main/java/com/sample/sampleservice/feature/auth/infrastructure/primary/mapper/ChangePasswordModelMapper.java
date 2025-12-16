package com.sample.sampleservice.feature.auth.infrastructure.primary.mapper;

import com.sample.sampleservice.feature.auth.api.rest.v1.model.ResetPasswordRequest;
import com.sample.sampleservice.feature.auth.domain.model.ChangePassword;
import com.sample.sampleservice.shared.mapper.infrastructure.primary.ModelMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChangePasswordModelMapper extends ModelMapper<ResetPasswordRequest, ChangePassword> {
}
