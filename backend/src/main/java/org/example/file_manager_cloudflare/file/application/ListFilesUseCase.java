package org.example.file_manager_cloudflare.file.application;

import org.example.file_manager_cloudflare.file.domain.FileMetadata;
import org.example.file_manager_cloudflare.file.domain.FilePage;
import org.example.file_manager_cloudflare.file.domain.UserId;
import org.example.file_manager_cloudflare.file.domain.port.FileMetadataRepository;
import org.springframework.stereotype.Service;

@Service
public class ListFilesUseCase {

    private final FileMetadataRepository metadataRepository;

    public ListFilesUseCase(FileMetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    public FilePage<FileMetadata> list(UserId ownerId, int pageNumber, int pageSize) {
        if (pageNumber < 0) {
            throw new IllegalArgumentException("page must be non-negative");
        }
        if (pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException("pageSize must be between 1 and 100");
        }
        return metadataRepository.findByOwnerId(ownerId, pageNumber, pageSize);
    }
}
