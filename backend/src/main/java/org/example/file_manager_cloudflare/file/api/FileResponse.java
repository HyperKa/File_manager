package org.example.file_manager_cloudflare.file.api;

import org.example.file_manager_cloudflare.file.domain.FileMetadata;
import org.example.file_manager_cloudflare.file.domain.FileStatus;

import java.time.Instant;
import java.util.UUID;

public record FileResponse(
        UUID id,
        String fileName,
        String mediaType,
        long sizeBytes,
        FileStatus status,
        Instant uploadedAt,
        String downloadUrl
) {

    public static FileResponse from(FileMetadata metadata) {
        UUID id = metadata.id().value();
        return new FileResponse(
                id,
                metadata.originalName().value(),
                metadata.mediaType().value(),
                metadata.sizeBytes(),
                metadata.status(),
                metadata.createdAt(),
                "/api/v1/files/" + id + "/content"
        );
    }
}
