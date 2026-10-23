package org.example.file_manager_cloudflare.file.application;

import org.example.file_manager_cloudflare.file.domain.FileId;
import org.example.file_manager_cloudflare.file.domain.FileMetadata;
import org.example.file_manager_cloudflare.file.domain.port.FileMetadataRepository;
import org.example.file_manager_cloudflare.file.domain.port.FileStoragePort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DeleteFileUseCase {

    private final FileMetadataRepository metadataRepository;
    private final FileStoragePort storagePort;

    public DeleteFileUseCase(FileMetadataRepository metadataRepository, FileStoragePort storagePort) {
        this.metadataRepository = metadataRepository;
        this.storagePort = storagePort;
    }

    @Transactional
    public void execute(FileId fileId) {
        // Поиск файла перед удалением (если нет - 404)
        FileMetadata metadata = metadataRepository.findFileById(fileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));

        // Удаление из S3
        storagePort.deleteFile(metadata.storageKey());

        // Удаление из БД
        metadataRepository.deleteFile(fileId);
    }
}
