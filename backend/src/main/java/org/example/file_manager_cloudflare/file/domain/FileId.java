package org.example.file_manager_cloudflare.file.domain;

import java.util.Objects;
import java.util.UUID;

// Обработка и сохранение идентификатора файла
public record FileId(UUID value) {

    public FileId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static FileId generateNewRandomUUID() {
        return new FileId(UUID.randomUUID());
    }

    public static FileId createFromUUID(UUID value) {
        return new FileId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
