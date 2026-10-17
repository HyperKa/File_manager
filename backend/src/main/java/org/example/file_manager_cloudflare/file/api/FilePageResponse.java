package org.example.file_manager_cloudflare.file.api;

import java.util.List;

public record FilePageResponse<FileResponse> (
            List<FileResponse> content,
            long totalElements,
            int totalPages
) {}


