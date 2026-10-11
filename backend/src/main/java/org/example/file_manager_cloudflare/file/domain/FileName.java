package org.example.file_manager_cloudflare.file.domain;

import java.util.Objects;

public record FileName(String value) {

    public FileName {
        Objects.requireNonNull(value, "value must not be null");
        value = normalize(value);
        if (value.isBlank()
                || value.contains("\n")
                || value.contains("\r")
                // ограничения по длине для обычных символов (обычный подсчет указателей) по 1-2 байта и эмодзи/иероглифов по 4 байта
                || value.length() > 255) {
            throw new RuntimeException("Invalid file name");
            // throw new InvalidFileException("Invalid file name");
        }
    }

    // взятие из всего пути только названия файла и типа: "filename.txt"
    private static String normalize(String originalName) {
        String replaced = Objects.requireNonNull(originalName).replace('\\', '/');
        int separator = replaced.lastIndexOf('/');
        return separator >= 0 ? replaced.substring(separator + 1) : replaced;
    }
}
