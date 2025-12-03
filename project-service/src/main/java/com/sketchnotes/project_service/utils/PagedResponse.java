package com.sketchnotes.project_service.utils;

import lombok.Builder;

import java.util.List;

@Builder
public record PagedResponse<T>(
        List<T> content,
        int pageNo,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean isLast
) {
}
