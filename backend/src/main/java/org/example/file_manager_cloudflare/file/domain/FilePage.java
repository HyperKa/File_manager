package org.example.file_manager_cloudflare.file.domain;

import java.util.List;

public record FilePage<T>(
        List<T> content,
        long totalElements,
        int pageNumber,
        int pageSize,
        int totalPages
) {
}
