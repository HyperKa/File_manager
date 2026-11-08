package org.example.file_manager_cloudflare.file.domain.port;

import org.example.file_manager_cloudflare.file.domain.*;

import java.util.Optional;

public interface FileMetadataRepository {
    FileMetadata createFile(FileMetadata metadata);
    Optional<FileMetadata> findFileById(FileId id);
    void saveFile(FileMetadata metadata);
    FilePage<FileMetadata> findAllFilesByOwnerId(UserId userId, int pageNumber, int pageSize);
    public void deleteFile(FileId id);
    void updateFileName(FileId fileId, FileName newName);

    // для повторной загрузки в UploadFileUseCase, если fileId занят:
    boolean existsById(FileId id);

    // поиск по sha256 для оптимизации загрузки
    Optional<FileMetadata> findBySha256(String sha256);
}
