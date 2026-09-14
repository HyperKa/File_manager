package org.example.file_manager_cloudflare.file.domain;

import org.example.file_manager_cloudflare.common.exception.InvalidFileException;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public record FileName(String value) {

    public FileName {
        Objects.requireNonNull(value, "value must not be null");
        value = normalize(value);
        if (value.isBlank()
                || value.contains("\n")
                || value.contains("\r")
                || value.codePointCount(0, value.length()) > 255
                || value.getBytes(StandardCharsets.UTF_8).length > 1024) {
            throw new InvalidFileException("Invalid file name");
        }
    }

    private static String normalize(String originalName) {
        String replaced = Objects.requireNonNull(originalName).replace('\\', '/');
        int separator = replaced.lastIndexOf('/');
        return separator >= 0 ? replaced.substring(separator + 1) : replaced;
    }
}
