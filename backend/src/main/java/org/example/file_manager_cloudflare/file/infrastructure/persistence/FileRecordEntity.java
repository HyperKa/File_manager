package org.example.file_manager_cloudflare.file.infrastructure.persistence;

import jakarta.persistence.*;
import org.example.file_manager_cloudflare.file.domain.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "file_metadata")
public class FileRecordEntity {

    @Id
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "storage_key", nullable = false)
    private String storageKey;

    @Column(name = "media_type", nullable = false)
    private String mediaType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "sha256")
    private String sha256;

    @Column(name = "etag")
    private String eTag;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FileStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected FileRecordEntity() {}

    // сохранение в базу
    public static FileRecordEntity fromDomain(FileMetadata metadata) {
        FileRecordEntity entity = new FileRecordEntity();
        entity.id = metadata.id().value();
        entity.ownerId = metadata.ownerId().value();
        entity.originalName = metadata.fileName().value();
        entity.storageKey = metadata.storageKey().value();
        entity.mediaType = metadata.mediaType().value();
        entity.sizeBytes = metadata.sizeBytes();
        entity.status = metadata.status();
        entity.createdAt = metadata.createdAt();
        entity.updatedAt = Instant.now();
        return entity;
    }

    // вывод из базы
    public FileMetadata toDomain() {
        return new FileMetadata(
                FileId.createFromUUID(id),
                UserId.createFromUUID(ownerId),
                new FileName(originalName),
                new StorageKey(storageKey),
                FileMediaType.parseOrDefault(mediaType),
                sizeBytes,
                sha256,
                eTag,
                status,
                createdAt,
                updatedAt,
                deletedAt,
                version
        );
    }

    public void setFileName(String fileName) {
        this.originalName = fileName;
    }

    public String getFileName() {
        return this.originalName;
    }
}