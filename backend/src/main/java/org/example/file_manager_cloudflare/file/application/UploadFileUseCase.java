package org.example.file_manager_cloudflare.file.application;

import org.example.file_manager_cloudflare.file.domain.*;
import org.example.file_manager_cloudflare.file.domain.port.FileMetadataRepository;
import org.example.file_manager_cloudflare.file.domain.port.FileStoragePort;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.print.attribute.standard.Media;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static liquibase.analytics.Event.Fields.command;

@Service
public class UploadFileUseCase {
    FileMetadataRepository metadataRepo;
    FileStoragePort storagePort;
    TransactionTemplate transactionTemplate;

    private static final Logger log = LoggerFactory.getLogger(UploadFileUseCase.class);
    private static final int MAX_RETRIES = 3;

    public UploadFileUseCase(FileMetadataRepository metadataRepo, FileStoragePort storagePort, TransactionTemplate transactionTemplate) {
        this.metadataRepo = metadataRepo;
        this.storagePort = storagePort;
        this.transactionTemplate = transactionTemplate;
    }

    private FileMetadata executeMacketFileAndSave(FileMetadata metadata, UploadFileCommand command) {

        // заполнение хэша для файла
        try {
            StoreCommand storeCommand = new StoreCommand(
                    metadata.storageKey(),
                    metadata.mediaType(),
                    metadata.sizeBytes()
            );
            // сохранение файла в S3 и возврат хэша
            StoredFile storedFile = storagePort.storeFile(command.content(), storeCommand);

            metadata.markReady(storedFile.eTag(), null);

            return transactionTemplate.execute(status -> {
                metadataRepo.saveFile(metadata);
                return metadata;
            });


        } catch (Exception e) {
            //
            transactionTemplate.executeWithoutResult( status -> {
                metadata.markFailed();
                metadataRepo.saveFile(metadata);
            });
            throw new RuntimeException("Ошибка при загрузке файла в S3", e);
        }

    }

    public FileMetadata uploadFile(UploadFileCommand command, UserId ownerId) {
        if (command.sizeBytes() == 0) {
            throw new IllegalArgumentException("<UploadFileUseCase>: File is empty");
        }

        FileId uniqueFileId = null;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            FileId candidateFileId = FileId.generateNewRandomUUID();

            if (metadataRepo.existsById(candidateFileId)) {
                log.warn("<UploadFileUseCase>: collision detected by id: {}.", candidateFileId.value());
                continue;
            }
            uniqueFileId = candidateFileId;
            log.info("<UploadFileUseCase>: Successfully generated unique FileId: {}", uniqueFileId.value());
            break;
        }

        if (uniqueFileId == null) {
            throw new IllegalArgumentException("<UploadFileUseCase>: uniqueFileId is empty");
        }

        StorageKey storageKey = new StorageKey("files/" + uniqueFileId.value());
        FileName fileName = new FileName(command.fileName());
        FileMediaType mediaType = validateBrowserMediaType(command);

        FileMetadata pending = FileMetadata.pending(
                ownerId,
                fileName,
                storageKey,
                mediaType,
                command.sizeBytes()
        );

        FileMetadata created = transactionTemplate.execute(status ->
                metadataRepo.createFile(pending));

        return executeMacketFileAndSave(created, command);
    }

    private FileMediaType validateBrowserMediaType(UploadFileCommand command) {
        String mediaType = command.mediaType();
        if (mediaType != null && !mediaType.isEmpty() && !mediaType.equals("application/octet-stream")) {
            return new FileMediaType(mediaType);
        }

        String filename = command.fileName().toLowerCase();
        if (filename.endsWith(".docx")) {
            return new FileMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        } else if (filename.endsWith(".doc")) {
            return new FileMediaType("application/msword");
        } else if (filename.endsWith(".txt")) {
            return new FileMediaType("text/plain");
        } else if (filename.endsWith(".pdf")) {
            return new FileMediaType("application/pdf");
        }

        throw new IllegalArgumentException("<UploadFileUseCase>: Неподдерживаемый тип файла от браузера: " + filename);
    }
}
