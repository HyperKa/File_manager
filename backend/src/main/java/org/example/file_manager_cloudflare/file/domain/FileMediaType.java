package org.example.file_manager_cloudflare.file.domain;

import org.example.file_manager_cloudflare.common.exception.InvalidFileException;

import java.util.Objects;
import java.util.regex.Pattern;

public record FileMediaType(String value) {

    private static final Pattern PATTERN = Pattern.compile(
            "^[a-zA-Z0-9!#$&^-_+|~]+/[a-zA-Z0-9!#$&^-_+|~.]+$"
    );

    public FileMediaType {
        Objects.requireNonNull(value, "value must not be null");
        value = value.trim().toLowerCase();
        if (!PATTERN.matcher(value).matches()) {
            throw new InvalidFileException("Invalid media type");
        }
    }

    public static FileMediaType of(String value) {
        if (value == null || value.isBlank()) {
            return new FileMediaType("application/octet-stream");
        }
        return new FileMediaType(value);
    }
}
