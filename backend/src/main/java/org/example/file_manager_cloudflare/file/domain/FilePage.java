package org.example.file_manager_cloudflare.file.domain;

import java.util.List;

/**
 *
 * @param content       - список файлов на странице
 * @param totalElements - общее количество файлов
 * @param pageNumber    - номер текущей страницы
 * @param pageSize      - размер страницы, ограничен системой, определяет кол-во файлов с веба для обработки (максимум 100 за запрос)
 * @param totalPages    - общее количество страниц
 * @param <T>           - объект FileMetadata
 *
 */

public record FilePage<T>(
        List<T> content,
        long totalElements,
        int pageNumber,
        int pageSize,
        int totalPages
) {
}
