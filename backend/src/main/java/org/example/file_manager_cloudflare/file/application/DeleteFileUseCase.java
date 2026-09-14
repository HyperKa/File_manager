package org.example.file_manager_cloudflare.file.application;

import org.example.file_manager_cloudflare.common.exception.FileNotFoundException;
import org.example.file_manager_cloudflare.file.domain.FileId;
import org.example.file_manager_cloudflare.file.domain.FileMetadata;
import org.example.file_manager_cloudflare.file.domain.UserId;
import org.example.file_manager_cloudflare.file.domain.port.FileMetadataRepository;
import org.example.file_manager_cloudflare.file.domain.port.FileStoragePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class DeleteFileUseCase {

    private final FileMetadataRepository metadataRepository;
    private final FileStoragePort storagePort;
    private final TransactionTemplate transactionTemplate;

    public DeleteFileUseCase(
            FileMetadataRepository metadataRepository,
            FileStoragePort storagePort,
            TransactionTemplate transactionTemplate
    ) {
        this.metadataRepository = metadataRepository;
        this.storagePort = storagePort;
        this.transactionTemplate = transactionTemplate;
    }

    public void delete(FileId id, UserId ownerId) {
        FileMetadata metadata = metadataRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException(id.value()));
        metadata.ensureOwnedBy(ownerId);

        markDeleting(metadata);
        try {
            storagePort.delete(metadata.storageKey());
            markDeleted(metadata);
        } catch (RuntimeException exception) {
            throw exception;
        }
    }

    private void markDeleting(FileMetadata metadata) {
        transactionTemplate.executeWithoutResult(status -> {
            FileMetadata current = metadataRepository.findById(metadata.id())
                    .orElseThrow(() -> new FileNotFoundException(metadata.id().value()));
            current.markDeleting();
            metadataRepository.save(current);
        });
    }

    private void markDeleted(FileMetadata metadata) {
        transactionTemplate.executeWithoutResult(status -> {
            FileMetadata current = metadataRepository.findById(metadata.id())
                    .orElseThrow(() -> new FileNotFoundException(metadata.id().value()));
            current.markDeleted();
            metadataRepository.save(current);
        });
    }
}
