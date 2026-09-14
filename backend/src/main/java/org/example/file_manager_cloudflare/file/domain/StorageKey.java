package org.example.file_manager_cloudflare.file.domain;

import java.util.Objects;

public record StorageKey(String value) {

    public StorageKey {
        Objects.requireNonNull(value, "value must not be null");
        value = value.trim();
        if (value.isBlank() || value.length() > 1024 || value.contains("..")) {
            throw new IllegalArgumentException("Invalid storage key");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
