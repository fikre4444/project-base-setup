package com.sample.sampleservice.shared.mapper.infrastructure.primary;

import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.sample.sampleservice.shared.mapper.infrastructure.domain.CommonMapper;

import java.util.List;
import java.util.Set;

/**
 * Contract for a generic dto to entity mapper.
 *
 * @param <D> - DTO type parameter.
 * @param <E> - Entity type parameter.
 */

public interface ModelMapper<D, E> extends CommonMapper {
    E toBo(D dto);

    D toDto(E entity);

    List<E> toBo(List<D> dtoList);

    List<D> toDto(List<E> entityList);

    List<D> toDto(Set<E> entityList);

    @Named("partialUpdate")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void partialUpdate(@MappingTarget E entity, D dto);
}
