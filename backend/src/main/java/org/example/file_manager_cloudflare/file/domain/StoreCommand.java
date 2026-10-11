package org.example.file_manager_cloudflare.file.domain;

import java.util.Objects;

public record StoreCommand (
        StorageKey key,
        FileMediaType mediaType,    // MIME-тип файла
        long sizeBytes
) {

    public StoreCommand {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(mediaType, "mediaType must not be null");
        if (sizeBytes < 0) {
            throw new IllegalArgumentException("sizeBytes must be non-negative");
        }
    }
}
