package com.sample.sampleservice.shared.mapper.infrastructure.secondary;

import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.sample.sampleservice.shared.mapper.infrastructure.domain.CommonMapper;
import com.sample.sampleservice.shared.pagination.domain.Page;

import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * Contract for a generic dto to entity mapper.
 *
 * @param <D> - DTO type parameter.
 * @param <E> - Entity type parameter.
 */

public interface EntityMapper<D, E> extends CommonMapper {
    E toEntity(D dto);

    D toBo(E entity);

    List<E> toEntity(List<D> dtoList);

    Set<E> toEntity(Set<D> dtoList);

    List<D> toBo(List<E> entityList);

    Set<D> toBo(Set<E> entityList);

    default Page<D> toBo(org.springframework.data.domain.Page<D> element) {
        return new Page<D>()
                .content(element.getContent())
                .total((int) element.getTotalElements())
                .totalPages(element.getTotalPages())
                .hasNext(element.hasNext())
                .hasPrevious(element.hasPrevious())
                .isLast(element.isLast())
                .currentPage(element.getPageable().getPageNumber() + 1)
                .isEmpty(element.isEmpty());
    }

    @Named("partialUpdate")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void partialUpdate(@MappingTarget E entity, D dto);

    @Named("file-name")
    default String fileName(String fileName) {

        String[] matches = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-5][0-9a-f]{3}-[089ab][0-9a-f]{3}-[0-9a-f]{12}_")
                .matcher(fileName)
                .results()
                .map(MatchResult::group)
                .toArray(String[]::new);
        if (matches.length >= 1) {
            String[] result = fileName.split(matches[0]);
            if (result.length == 2) {
                fileName = result[1];
            }
        }

        return fileName;
    }
}
