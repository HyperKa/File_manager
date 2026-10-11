package org.example.file_manager_cloudflare.file.domain;

import org.example.file_manager_cloudflare.common.exception.FileAccessDeniedException;
import org.example.file_manager_cloudflare.common.exception.FileStateException;

import java.time.Instant;
import java.util.Objects;

public class FileMetadata {

    private final FileId id;
    private final UserId ownerId;
    private FileName originalName;
    private final StorageKey storageKey;
    private final FileMediaType mediaType;
    private long sizeBytes;
    private String sha256;
    private String eTag;
    private FileStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
    private long version;

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

    public static FileMetadata pending(
            UserId ownerId,
            FileName originalName,
            StorageKey storageKey,
            FileMediaType mediaType,
            long sizeBytes
    ) {
        return new FileMetadata(
                FileId.generate(),
                ownerId,
                originalName,
                storageKey,
                mediaType,
                sizeBytes,
                null,
                null,
                FileStatus.PENDING,
                null,
                null,
                null,
                0
        );
    }

    public void markReady(String eTag, String sha256) {
        requireStatus(FileStatus.PENDING, "Only a pending file can be marked as ready");
        this.eTag = eTag;
        this.sha256 = sha256;
        this.status = FileStatus.READY;
    }

    // применить при пересылке и проверке сохранения в бд
    public boolean markFailed() {
        // requireStatus(FileStatus.PENDING, "Only a pending file can be marked as failed");
        if (FileStatus.FAILED.equals(status)) {
            this.status = FileStatus.FAILED;
            return true;
        }
        return false;
    }

    public void markDeleting() {
        requireStatus(FileStatus.READY, "Only a ready file can be marked as deleting");
        this.status = FileStatus.DELETING;
    }

    public void markDeleted() {
        requireStatus(FileStatus.DELETING, "Only a deleting file can be marked as deleted");
        this.status = FileStatus.DELETED;
        this.deletedAt = Instant.now();
    }

    // id автора соответствует чтению (загрузке)?
    public void ensureReadableBy(UserId userId) {
        ensureOwnedBy(userId);
        if (status != FileStatus.READY) {
            throw new FileStateException("File is not available for download");
        }
    }

    // id автора соответствует удалению, изменению?
    public void ensureOwnedBy(UserId userId) {
        if (!ownerId.equals(userId)) {
            throw new FileAccessDeniedException();
        }
    }

    // проверка статуса перед выставлением нового статуса
    private void requireStatus(FileStatus expected, String message) {
        if (status != expected) {
            throw new FileStateException(message);
        }
    }

    public FileId id() {
        return id;
    }

    public UserId ownerId() {
        return ownerId;
    }

    public FileName originalName() {
        return originalName;
    }

    public StorageKey storageKey() {
        return storageKey;
    }

    public FileMediaType mediaType() {
        return mediaType;
    }

    public void rename(FileName newName) {
        if (this.status != FileStatus.READY) {
            throw new FileStateException("Only ready files can be renamed");
        }
        this.originalName = Objects.requireNonNull(newName);
    }

    public long sizeBytes() {
        return sizeBytes;
    }

    public String sha256() {
        return sha256;
    }

    public String eTag() {
        return eTag;
    }

    public FileStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public Instant deletedAt() {
        return deletedAt;
    }

    public long version() {
        return version;
    }
}
