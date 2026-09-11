package org.example.file_manager_cloudflare.repo;

import org.example.file_manager_cloudflare.entity.FileRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<FileRecord, Long> {
}