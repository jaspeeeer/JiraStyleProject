package com.standardinsurance.intrack.common.web;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Standard envelope for paginated responses (CLAUDE.md §8). Wraps Spring Data {@link Page}
 * into a stable API shape decoupled from Spring types.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
