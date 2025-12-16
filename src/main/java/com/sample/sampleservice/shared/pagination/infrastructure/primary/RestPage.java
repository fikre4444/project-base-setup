package com.sample.sampleservice.shared.pagination.infrastructure.primary;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Schema(name = "Page", description = "Paginated content")
public final class RestPage<T> {

    private final List<T> content;
    private final int currentPage;
    private final int pageSize;
    private final long totalElementsCount;
    private final int pagesCount;
    private final boolean hasPrevious;
    private final boolean hasNext;
}
