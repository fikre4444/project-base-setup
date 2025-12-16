package com.sample.sampleservice.shared.pagination.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.sample.sampleservice.shared.error.domain.Assert;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class Pageable {

    private final int page;
    private final int pageSize;
    private final int offset;
    private final String sortBy;
    private final Direction direction;

    public Pageable(int page, int pageSize, String sortBy, Direction direction) {
        Assert.field("page", page).min(0);
        Assert.field("pageSize", pageSize).min(1).max(100);

        this.sortBy = sortBy;
        this.direction = direction;
        this.page = page;
        this.pageSize = pageSize;
        offset = page * pageSize;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(page).append(pageSize).build();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Pageable other = (Pageable) obj;
        return new EqualsBuilder().append(page, other.page).append(pageSize, other.pageSize).build();
    }

    public enum Direction {
        ASC, DESC
    }
}
