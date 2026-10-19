package org.example.file_manager_cloudflare.file.application;

import jakarta.transaction.Transactional;
import org.example.file_manager_cloudflare.file.api.RenameFileRequest;
import org.example.file_manager_cloudflare.file.domain.FileId;
import org.example.file_manager_cloudflare.file.domain.FileMetadata;
import org.example.file_manager_cloudflare.file.domain.FileName;
import org.example.file_manager_cloudflare.file.domain.port.FileMetadataRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RenameFileUseCase {
    private final FileMetadataRepository metadataRepository;
    private final TransactionTemplate transactionTemplate;

    public RenameFileUseCase(FileMetadataRepository metadataRepository, TransactionTemplate transactionTemplate) {
        this.metadataRepository = metadataRepository;
        this.transactionTemplate = transactionTemplate;
    }

    @Transactional
    public FileMetadata execute(FileId fileId, String newName) {
        FileMetadata metadata = metadataRepository.findFileById(fileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));

        FileName newFileName = new FileName(newName);

        metadata.rename(newFileName);
        metadataRepository.saveFile(metadata);
        return metadata;
    }

    // Версия через TransactionTemplate, то же самое, только отлов исключений явный, с сохранением метрик сбоя сервера
    // в будущем
    public FileMetadata executeTempl(FileId fileId, RenameFileRequest request) {
        return transactionTemplate.execute(status -> {
            try {
                FileMetadata metadata = metadataRepository.findFileById(fileId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));

                metadata.rename(new FileName(request.newName()));
                metadataRepository.saveFile(metadata);
                return metadata;

            } catch (Exception e) {
                status.setRollbackOnly();
                System.out.println("Ошибка при сохранении, транзакция отменена: " + e.getMessage());
                return null;
            }
        });
    }
}
