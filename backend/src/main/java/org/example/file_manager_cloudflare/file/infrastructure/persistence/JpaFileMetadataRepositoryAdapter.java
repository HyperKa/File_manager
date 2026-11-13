package org.example.file_manager_cloudflare.file.infrastructure.persistence;

import org.example.file_manager_cloudflare.file.domain.*;
import org.example.file_manager_cloudflare.file.domain.port.FileMetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class JpaFileMetadataRepositoryAdapter implements FileMetadataRepository {
    private static final Logger log = LoggerFactory.getLogger(JpaFileMetadataRepositoryAdapter.class);

    private final FileRecordJpaRepository jpaRepository;

    public JpaFileMetadataRepositoryAdapter(FileRecordJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public FileMetadata createFile(FileMetadata metadata) {
        FileRecordEntity entity = FileRecordEntity.fromDomain(metadata);
        return jpaRepository.save(entity).toDomain();
    }

    /**
     * ГЛАВНАЯ ФИЧА SOFT DELETE: findById НЕ ВИДИТ УДАЛЁННЫЕ ФАЙЛЫ
     * UseCase думает, что файл реально удалён, хотя в БД он есть с deletedAt != null
     */
    @Override
    public Optional<FileMetadata> findFileById(FileId id) {
        return jpaRepository.findByFileIdAndDeletedAtIsNull(id.value())
                .map(FileRecordEntity::toDomain);
    }

    @Override
    public Optional<FileMetadata> findBySha256(String sha256) {
        return jpaRepository.findBySha256(sha256).map(FileRecordEntity::toDomain);
    }

    @Override
    public boolean existsById(FileId id) {
        return jpaRepository.existsById(id.value());
    }

    @Override
    public void saveFile(FileMetadata metadata) {
        FileRecordEntity entity = FileRecordEntity.fromDomain(metadata);
        jpaRepository.save(entity);
    }

    @Override
    public FilePage<FileMetadata> findAllFilesByOwnerId(UserId userId, int pageNumber, int pageSize) {
        PageRequest pageRequest = PageRequest.of(
                pageNumber,
                pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        // Фильтрация удалённых файлов — на уровне JPA
        Page<FileRecordEntity> page = jpaRepository.findByOwnerIdAndDeletedAtIsNull(userId.value(), pageRequest);

        return new FilePage<>(
                page.getContent().stream().map(FileRecordEntity::toDomain).toList(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize()
        );
    }

    /**
     * SOFT DELETE: помечаем файл, НЕ удаляя из БД и S3
     */
    @Override
    public void markAsDeleted(FileId id) {
        int updatedRows = jpaRepository.markAsDeleted(
                id.value(),
                java.time.Instant.now(),
                FileStatus.DELETED
        );
        log.info("<JpaAdapter>: markAsDeleted fileId={}, updatedRows={}", id.value(), updatedRows);
        if (updatedRows == 0) {
            log.warn("<JpaAdapter>: no rows updated for fileId={} (возможно, уже удалён)", id.value());
        }
    }

}
