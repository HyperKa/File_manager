package org.example.file_manager_cloudflare.file.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FileRecordJpaRepository extends JpaRepository<FileRecordEntity, UUID> {

}
