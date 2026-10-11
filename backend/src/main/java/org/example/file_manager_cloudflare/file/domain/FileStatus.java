package org.example.file_manager_cloudflare.file.domain;

public enum FileStatus {
    PENDING,            // Загружается (в процессе)
    READY,              // Успешно загружен и доступен для скачивания
    FAILED,             // Ошибка загрузки
    DELETING,           // В процессе удаления
    DELETED             // Удалён (мягкое удаление)
}
