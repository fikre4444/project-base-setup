package com.sample.sampleservice.feature.auth.infrastructure.primary.mapper;

import com.sample.sampleservice.feature.auth.api.rest.v1.model.UserDetail;
import com.sample.sampleservice.feature.auth.domain.model.UserDetails;
import com.sample.sampleservice.shared.mapper.infrastructure.primary.ModelMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserDetailModelMapper extends ModelMapper<UserDetail, UserDetails> {
}
