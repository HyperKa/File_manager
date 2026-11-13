package org.example.file_manager_cloudflare.file.infrastructure.persistence;

import org.example.file_manager_cloudflare.file.domain.FileStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileRecordJpaRepository extends JpaRepository<FileRecordEntity, UUID> {

    Page<FileRecordEntity> findByOwnerIdAndDeletedAtIsNull(UUID userId, Pageable pageable);

    Optional<FileRecordEntity> findBySha256(String sha256);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE FileRecordEntity e " +
            "SET    e.deletedAt = :deletedAt, " +
            "       e.status = :status, " +
            "       e.updatedAt = :deletedAt " +
            "WHERE e.fileId = :id AND e.deletedAt IS NULL")
    int markAsDeleted(@Param("id") UUID id,
                      @Param("deletedAt") Instant deletedAt,
                      @Param("status") FileStatus status);

    // ⚠️ ВАЖНО: имена полей Entity! У тебя поле называется fileId, ownerId.
    Optional<FileRecordEntity> findByFileIdAndDeletedAtIsNull(UUID fileId);
    List<FileRecordEntity> findAllByOwnerIdAndDeletedAtIsNull(UUID ownerId);
}
