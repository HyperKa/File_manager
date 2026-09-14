package org.example.file_manager_cloudflare.file.domain;

public record StoredFile(
        StorageKey key,
        String eTag,
        long sizeBytes
) {
}
