package org.example.file_manager_cloudflare.file.domain;

public record StoredFile(
        StorageKey key,
        String eTag,            // Контрольная сумма файла
        long sizeBytes
) {
}
