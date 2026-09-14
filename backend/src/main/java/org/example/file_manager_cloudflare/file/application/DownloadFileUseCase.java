package org.example.file_manager_cloudflare.file.application;

import org.example.file_manager_cloudflare.common.exception.FileNotFoundException;
import org.example.file_manager_cloudflare.file.domain.FileId;
import org.example.file_manager_cloudflare.file.domain.FileMetadata;
import org.example.file_manager_cloudflare.file.domain.UserId;
import org.example.file_manager_cloudflare.file.domain.port.FileMetadataRepository;
import org.example.file_manager_cloudflare.file.domain.port.FileStoragePort;
import org.springframework.stereotype.Service;

@Service
public class DownloadFileUseCase {

    private final FileMetadataRepository metadataRepository;
    private final FileStoragePort storagePort;

    public DownloadFileUseCase(
            FileMetadataRepository metadataRepository,
            FileStoragePort storagePort
    ) {
        this.metadataRepository = metadataRepository;
        this.storagePort = storagePort;
    }

    public FileDownload open(FileId id, UserId ownerId) {
        FileMetadata metadata = metadataRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException(id.value()));
        metadata.ensureReadableBy(ownerId);
        return new FileDownload(metadata, storagePort.open(metadata.storageKey()));
    }
}
