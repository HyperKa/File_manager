package org.example.file_manager_cloudflare.file.application;

import java.io.InputStream;
import java.util.Objects;

public record UploadFileCommand(
        InputStream content,
        String fileName,
        String mediaType,
        long sizeBytes
) {

    public UploadFileCommand {
        Objects.requireNonNull(content, "content must not be null");
        Objects.requireNonNull(fileName, "fileName must not be null");
        if (sizeBytes < 0) {
            throw new IllegalArgumentException("sizeBytes must be non-negative");
        }
    }
}
