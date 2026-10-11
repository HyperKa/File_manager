package org.example.file_manager_cloudflare.file.domain;

import java.util.Objects;

/**
 * Команда для загрузки файла. S3 бакету необходимо передать данные на вход контроллеру:
 *
 * @param key       - путь, по которому нужно внутри бакета сохранить файл
 *
 * @param mediaType - какой заголовок выставить, обозначающий
 *                    тип файла (image/png, image/jpeg, неизвестный тип: application/octet-stream)
 *
 * @param sizeBytes - размер в байтах
 */
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
