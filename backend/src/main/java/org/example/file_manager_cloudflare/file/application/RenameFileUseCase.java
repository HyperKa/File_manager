package org.example.file_manager_cloudflare.file.application;

import org.example.file_manager_cloudflare.common.exception.FileNotFoundException;
import org.example.file_manager_cloudflare.file.domain.FileId;
import org.example.file_manager_cloudflare.file.domain.FileMetadata;
import org.example.file_manager_cloudflare.file.domain.FileName;
import org.example.file_manager_cloudflare.file.domain.UserId;
import org.example.file_manager_cloudflare.file.domain.port.FileMetadataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class RenameFileUseCase {

    private final FileMetadataRepository metadataRepository;
    private final TransactionTemplate transactionTemplate;

    public RenameFileUseCase(FileMetadataRepository metadataRepository, TransactionTemplate transactionTemplate) {
        this.metadataRepository = metadataRepository;
        this.transactionTemplate = transactionTemplate;
    }

    public FileMetadata rename(FileId id, UserId ownerId, String newName) {
        return transactionTemplate.execute(status -> {
            FileMetadata metadata = metadataRepository.findById(id)
                    .orElseThrow(() -> new FileNotFoundException(id.value()));
            metadata.ensureOwnedBy(ownerId);
            metadata.rename(new FileName(newName));
            metadataRepository.save(metadata);
            return metadata;
        });
    }
}