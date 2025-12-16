package com.sample.sampleservice.feature.auth.infrastructure.primary.mapper;

import com.sample.sampleservice.feature.auth.api.rest.v1.model.Token;
import com.sample.sampleservice.feature.auth.domain.model.OAuth2TokenResult;
import com.sample.sampleservice.shared.mapper.infrastructure.primary.ModelMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TokenModelMapper extends ModelMapper<Token, OAuth2TokenResult> {
}
