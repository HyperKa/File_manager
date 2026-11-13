package org.example.file_manager_cloudflare.file.domain;

/**
 * DTO для хранения информации о загруженном файле, возвращает eTag при успехе сохранения
 * @param key       - по какому пути внутри бакета сохранен файл
 *
 * @param eTag      - хэш-код сохраненного файла для валидации уникальности файла
 *
 * @param sizeBytes - размер сохраненного файла в байтах
 */

public record StoredFile(
        StorageKey key,
        String eTag,            // Контрольная сумма файла
        long sizeBytes
) {
}
