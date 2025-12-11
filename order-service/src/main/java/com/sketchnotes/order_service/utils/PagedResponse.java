package com.sketchnotes.order_service.utils;

import java.util.List;

import lombok.Builder;
import org.springframework.data.domain.Page;
@Builder
public record PagedResponse<T>(
        List<T> content,
        int pageNo,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean isLast
) {
    /**
     * Convert từ Spring Data Page sang PagedResponse
     */
    public static <T> PagedResponse<T> fromPage(Page<T> page) {
        return new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
