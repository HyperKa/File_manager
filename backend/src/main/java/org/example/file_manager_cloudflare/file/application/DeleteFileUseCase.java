package org.example.file_manager_cloudflare.file.application;

import org.example.file_manager_cloudflare.file.domain.FileId;
import org.example.file_manager_cloudflare.file.domain.FileMetadata;
import org.example.file_manager_cloudflare.file.domain.port.FileMetadataRepository;
import org.example.file_manager_cloudflare.file.domain.port.FileStoragePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.example.file_manager_cloudflare.common.exception.FileNotFoundException;

@Service
public class DeleteFileUseCase {

    private final FileMetadataRepository metadataRepository;
    private final FileStoragePort storagePort;
    private final TransactionTemplate transactionTemplate;
    private static final Logger log = LoggerFactory.getLogger(DeleteFileUseCase.class);

    public DeleteFileUseCase(FileMetadataRepository metadataRepository, FileStoragePort storagePort,TransactionTemplate transactionTemplate) {
        this.metadataRepository = metadataRepository;
        this.storagePort = storagePort;
        this.transactionTemplate = transactionTemplate;
    }

    @Transactional
    public void execute(FileId fileId) {
        // Поиск файла перед удалением (если нет - 404)
        FileMetadata metadata = metadataRepository.findFileById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File with id '" + fileId.value() + "' not found"));

        transactionTemplate.executeWithoutResult(status -> {
            metadataRepository.markAsDeleted(fileId);
        });

        log.info("<DeleteFileUseCase>: file marked as deleted: {}", fileId.value());

    }
}
