package org.example.file_manager_cloudflare.file.application;

import org.example.file_manager_cloudflare.file.domain.*;
import org.example.file_manager_cloudflare.file.domain.port.FileMetadataRepository;
import org.example.file_manager_cloudflare.file.domain.port.FileStoragePort;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.print.attribute.standard.Media;

@Service
public class UploadFileUseCase {
    FileMetadataRepository metadataRepo;
    FileStoragePort storagePort;
    TransactionTemplate transactionTemplate;

    public UploadFileUseCase(FileMetadataRepository metadataRepo, FileStoragePort storagePort, TransactionTemplate transactionTemplate) {
        this.metadataRepo = metadataRepo;
        this.storagePort = storagePort;
        this.transactionTemplate = transactionTemplate;
    }

    public FileMetadata uploadFile(UploadFileCommand command, UserId ownerId) {

        FileId fileId = FileId.generateNewRandomUUID();
        FileName fileName = new FileName(command.fileName());
        FileMediaType mediaType = validateBrowserMediaType(
                command,
                command.mediaType());
        StorageKey storageKey = new StorageKey("files/" + fileId.value());

        FileMetadata pending = FileMetadata.pending(
                ownerId, fileName, storageKey, mediaType, command.sizeBytes()
        );

        // сохранение макета файла на основе загруженных данных
        FileMetadata created = transactionTemplate.execute(status ->
                metadataRepo.createFile(pending)
        );

        // заполнение хэша для файла
        try {
            StoreCommand storeCommand = new StoreCommand(storageKey, mediaType, command.sizeBytes());
            // сохранение файла в S3 и возврат хэша
            StoredFile storedFile = storagePort.storeFile(command.content(), storeCommand);

            created.markReady(storedFile.eTag(), null);

            return transactionTemplate.execute(status -> {
                metadataRepo.saveFile(created);
                return created;
            });


        } catch (Exception e) {
            //
            transactionTemplate.executeWithoutResult( status -> {
                created.markFailed();
                metadataRepo.saveFile(created);
            });
            throw new RuntimeException("Ошибка при загрузке файла в S3", e);
        }

    }

    private FileMediaType validateBrowserMediaType(UploadFileCommand command, String mediaType) {
        if (mediaType == null || mediaType.isEmpty() || mediaType.equals("application/octet-stream")) {
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
        }
        return new FileMediaType("application/octet-stream");
    }
}
