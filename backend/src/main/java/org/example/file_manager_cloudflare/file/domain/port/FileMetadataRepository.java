package org.example.file_manager_cloudflare.file.domain.port;

import org.example.file_manager_cloudflare.file.domain.FileId;
import org.example.file_manager_cloudflare.file.domain.FileMetadata;
import org.example.file_manager_cloudflare.file.domain.FilePage;
import org.example.file_manager_cloudflare.file.domain.UserId;

import java.util.Optional;

public interface FileMetadataRepository {

    FileMetadata create(FileMetadata metadata);

    Optional<FileMetadata> findFileById(FileId id);

    FilePage<FileMetadata> findByOwnerId(UserId ownerId, int pageNumber, int pageSize);

    void save(FileMetadata metadata);
}
