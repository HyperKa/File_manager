package org.example.file_manager_cloudflare.file.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FileRecordJpaRepository extends JpaRepository<FileRecordEntity, UUID> {

    Page<FileRecordEntity> findByOwnerIdAndDeletedAtIsNull(UUID ownerId, Pageable pageable);
}
