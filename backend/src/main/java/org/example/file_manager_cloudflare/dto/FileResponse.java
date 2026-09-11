package org.example.file_manager_cloudflare.dto;

public record FileResponse(
        Long id,
        String fileName,
        String downloadUrl,
        String fileType,
        String fileSize
) {}
