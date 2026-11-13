package org.example.file_manager_cloudflare.file.application;

import io.micrometer.common.util.internal.logging.Slf4JLoggerFactory;
import org.example.file_manager_cloudflare.file.domain.*;
import org.example.file_manager_cloudflare.file.domain.port.FileMetadataRepository;
import org.example.file_manager_cloudflare.file.domain.port.FileStoragePort;
import org.example.file_manager_cloudflare.file.infrastructure.persistence.FileRecordEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;

@Service
public class UploadFileUseCase {

    private static final Logger log = LoggerFactory.getLogger(UploadFileUseCase.class);
    private static final int MAX_RETRIES = 3;
    private static final long DEDUPLICATION_LIMIT_BYTES = 5 * 1024 * 1024; // 5 МБ

    private final FileMetadataRepository metadataRepo;
    private final FileStoragePort storagePort;
    private final TransactionTemplate transactionTemplate;

    public UploadFileUseCase(FileMetadataRepository metadataRepo, FileStoragePort storagePort, TransactionTemplate transactionTemplate) {
        this.metadataRepo = metadataRepo;
        this.storagePort = storagePort;
        this.transactionTemplate = transactionTemplate;
    }

    public FileMetadata uploadFile(UploadFileCommand command, UserId ownerId) {
        if (command.sizeBytes() == 0) {
            throw new IllegalArgumentException("<UploadFileUseCase>: File is empty");
        }

        // 1. Retry-цикл для FileId (BE-1)
        FileId uniqueFileId = getUniqueFileId();

        // 2. Создание черновика
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

        // Создание записи в бд
        FileMetadata created = transactionTemplate.execute(status ->
                metadataRepo.createFile(pending)
        );

        // Логика дедупликации и загрузки
        String sha256 = null;
        String eTag = null;
        StorageKey finalStorageKey = storageKey;

        if (command.sizeBytes() <= DEDUPLICATION_LIMIT_BYTES) {
            try {
                // Чтение потока в память (безопасно, т.к. <= 5 МБ)
                byte[] fileBytes = command.content().readAllBytes();

                // Подсчет хэша
                sha256 = Sha256Calculator.calculate(new ByteArrayInputStream(fileBytes));
                log.info("<UploadFileUseCase>: Calculated SHA256: {}", sha256);

                // Проверка дедупликации
                Optional<FileMetadata> existingFile = metadataRepo.findBySha256(sha256);

                // Логика дедупликации
                if (existingFile.isPresent()) {
                    FileMetadata duplicate = existingFile.get();
                    finalStorageKey = duplicate.storageKey();
                    eTag = duplicate.eTag(); // Берём eTag оригинала
                    log.info("<UploadFileUseCase>: Deduplication hit! Reusing storageKey: {}", finalStorageKey.value());
                } else {
                    // НОВЫЙ файл. НОВЫЙ поток из тех же байтов для S3
                    InputStream uploadStream = new ByteArrayInputStream(fileBytes);
                    StoredFile storedFile = storagePort.storeFile(uploadStream, new StoreCommand(storageKey, mediaType, command.sizeBytes()));
                    eTag = storedFile.eTag();
                }
            } catch (Exception e) {
                handleUploadFailure(created, e);
            }
        } else {
            // Файл > 5 МБ. Без дедупликации, возврат оригинального потока как есть
            try {
                StoredFile storedFile = storagePort.storeFile(command.content(), new StoreCommand(storageKey, mediaType, command.sizeBytes()));
                eTag = storedFile.eTag();
            } catch (Exception e) {
                handleUploadFailure(created, e);
            }
        }

        // Сохранение финального статуса
        created.markReady(eTag, sha256);
        transactionTemplate.executeWithoutResult(status -> metadataRepo.saveFile(created));

        return created;
    }

    private FileId getUniqueFileId() {
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
            throw new RuntimeException("<UploadFileUseCase>: Failed to generate a unique FileId after " + MAX_RETRIES + " attempts.");
        }
        return uniqueFileId;
    }


    private void handleUploadFailure(FileMetadata metadata, Exception e) {
        log.error("<UploadFileUseCase>: S3 upload failed for FileId: {}", metadata.id().value(), e);
        transactionTemplate.executeWithoutResult(status -> {
            metadata.markFailed();
            metadataRepo.saveFile(metadata);
        });
        throw new RuntimeException("Ошибка при загрузке файла в S3", e);
    }

    private FileMediaType validateBrowserMediaType(UploadFileCommand command) {
        String mediaType = command.mediaType();
        if (mediaType != null && !mediaType.isEmpty() && !mediaType.equals("application/octet-stream")) {
            return new FileMediaType(mediaType);
        }

        String filename = command.fileName().toLowerCase();
        if (filename.endsWith(".docx")) return new FileMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        if (filename.endsWith(".doc")) return new FileMediaType("application/msword");
        if (filename.endsWith(".txt")) return new FileMediaType("text/plain");
        if (filename.endsWith(".pdf")) return new FileMediaType("application/pdf");

        throw new IllegalArgumentException("<UploadFileUseCase>: Неподдерживаемый тип файла от браузера: " + filename);
    }
}