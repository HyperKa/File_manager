package org.example.file_manager_cloudflare.file.api;

import org.example.file_manager_cloudflare.file.domain.FileMetadata;
import org.example.file_manager_cloudflare.file.domain.FilePage;

import java.util.List;

public record FilePageResponse(
        List<FileResponse> content,
        long totalElements,
        int pageNumber,
        int pageSize,
        int totalPages
) {

    public static FilePageResponse from(FilePage<FileMetadata> page) {
        return new FilePageResponse(
                page.content().stream().map(FileResponse::from).toList(),
                page.totalElements(),
                page.pageNumber(),
                page.pageSize(),
                page.totalPages()
        );
    }
}
