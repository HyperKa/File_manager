package org.example.file_manager_cloudflare.file.application;

import org.example.file_manager_cloudflare.file.domain.FileId;
import org.example.file_manager_cloudflare.file.domain.FileMetadata;
import org.example.file_manager_cloudflare.file.domain.port.FileMetadataRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DownloadFileUseCase {

    private final FileMetadataRepository metadataRepository;

    public DownloadFileUseCase(FileMetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    public Optional<FileMetadata> execute(FileId fileId) {
        return metadataRepository.findFileById(fileId);
    }
}