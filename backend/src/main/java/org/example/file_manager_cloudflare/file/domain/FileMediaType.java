package org.example.file_manager_cloudflare.file.domain;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Обработка и сохранение пути файла
/**
 *
 * @param value - подается на вход MIME-тип из HTTP-запроса
 *
 * Когда браузер скачивает или загружает файл, он ориентируется не на расширение .docx или .mp3, а на MIME-тип:
 * Для .mp3 это audio/mpeg
 * Для .docx это application/vnd.openxmlformats-officedocument.wordprocessingml.document
 * Для .png это image/png
 * Для .svg это image/svg+xml
 */

public record FileMediaType(String value) {

    private static final Pattern PATTERN = Pattern.compile(
            "^[a-zA-Z0-9!#$&^-_+|~]+/[a-zA-Z0-9!#$&^-_+|~.]+$"
    );

    public FileMediaType {
        Objects.requireNonNull(value, "value must not be null");
        value = value.trim().toLowerCase();
        Matcher text = PATTERN.matcher(value);
        if (!text.matches()) {
            throw new RuntimeException("Invalid media type");
            // throw new InvalidFileException("Invalid media type");
        }
    }

    public static FileMediaType parseOrDefault(String value) {
        if (value == null || value.isBlank()) {
            return new FileMediaType("application/octet-stream");
        }
        return new FileMediaType(value);
    }
}
