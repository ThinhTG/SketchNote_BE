package com.sketchnotes.identityservice.ultils;

import java.util.List;

public record PagedResponse<T>(
        List<T> content,
        int pageNo,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean isLast
) {
}
