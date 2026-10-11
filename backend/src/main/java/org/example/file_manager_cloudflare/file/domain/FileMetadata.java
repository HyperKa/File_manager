package org.example.file_manager_cloudflare.file.domain;

import java.time.Instant;
import java.util.Objects;

public class FileMetadata {

    // Неизменные поля (final)
    private final FileId id;
    private final UserId ownerId;
    private final StorageKey storageKey;
    private final FileMediaType mediaType;
    private final long sizeBytes;
    private final Instant createdAt;

    // Изменяемые поля
    private FileName originalName;
    private String sha256;
    private String eTag;
    private FileStatus status;
    private Instant updatedAt;
    private Instant deletedAt;
    private long version;

    // Полный конструктор (для восстановления из БД)
    public FileMetadata(
            FileId id,
            UserId ownerId,
            FileName originalName,
            StorageKey storageKey,
            FileMediaType mediaType,
            long sizeBytes,
            String sha256,
            String eTag,
            FileStatus status,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt,
            long version
    ) {
        this.id = Objects.requireNonNull(id);
        this.ownerId = Objects.requireNonNull(ownerId);
        this.originalName = Objects.requireNonNull(originalName);
        this.storageKey = Objects.requireNonNull(storageKey);
        this.mediaType = Objects.requireNonNull(mediaType);
        this.sizeBytes = sizeBytes;
        this.sha256 = sha256;
        this.eTag = eTag;
        this.status = Objects.requireNonNull(status);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.version = version;
    }

    // Фабрика для черновика (при старте загрузки)
    public static FileMetadata pending(
            UserId ownerId,
            FileName originalName,
            StorageKey storageKey,
            FileMediaType mediaType,
            long sizeBytes
    ) {
        return new FileMetadata(
                FileId.generateNewRandomUUID(),
                ownerId,
                originalName,
                storageKey,
                mediaType,
                sizeBytes,
                null,
                null,
                FileStatus.PENDING,
                Instant.now(),
                Instant.now(),
                null,
                0
        );
    }

    // Методы мутации состояния:
    public void markReady(String eTag, String sha256) {
        this.eTag = eTag;
        this.sha256 = sha256;
        this.status = FileStatus.READY;
        this.updatedAt = Instant.now();
    }

    public void markFailed() {
        this.status = FileStatus.FAILED;
        this.updatedAt = Instant.now();
    }

    public void rename(FileName newName) {
        this.originalName = Objects.requireNonNull(newName);
        this.updatedAt = Instant.now();
    }

    // Геттеры для FileResponse:
    public FileId id() { return id; }
    public UserId ownerId() { return ownerId; }
    public FileName fileName() { return originalName; }
    public StorageKey storageKey() { return storageKey; }
    public FileMediaType mediaType() { return mediaType; }
    public long sizeBytes() { return sizeBytes; }
    public FileStatus status() { return status; }
    public Instant createdAt() { return createdAt; }
}