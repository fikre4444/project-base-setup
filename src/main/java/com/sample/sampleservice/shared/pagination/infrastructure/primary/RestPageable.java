package com.sample.sampleservice.shared.pagination.infrastructure.primary;

import com.sample.sampleservice.shared.pagination.domain.Pageable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Valid
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Schema(name = "JhipsterSampleApplicationPageable", description = "Pagination information")
public class RestPageable {

    @Min(value = 0)
    private int page;

    @Builder.Default
    @Min(value = 1)
    @Max(value = 100)
    private int pageSize = 10;

    private String sortBy;

    private Pageable.Direction direction;

    public Pageable toPageable() {
        return new Pageable(page, pageSize, sortBy, direction);
    }
}
