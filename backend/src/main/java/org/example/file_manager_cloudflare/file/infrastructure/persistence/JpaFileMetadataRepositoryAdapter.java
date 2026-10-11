package org.example.file_manager_cloudflare.file.infrastructure.persistence;

import org.example.file_manager_cloudflare.file.domain.*;

import java.util.Optional;

public class JpaFileMetadataRepositoryAdapter {
    private final FileRecordJpaRepository fileRecordJpaRepository;

    public JpaFileMetadataRepositoryAdapter(FileRecordJpaRepository fileRecordJpaRepository) {
        this.fileRecordJpaRepository = fileRecordJpaRepository;
    }

    public FileMetadata create(FileMetadata metadata) {
        FileRecordEntity entity = FileRecordEntity.fromDomain(metadata);
        return fileRecordJpaRepository.save(entity).toDomain();
    }

    public Optional<FileMetadata> findById(FileId id) {
        return fileRecordJpaRepository.findById(id.value()).map(FileRecordEntity::toDomain);
    }

    public void save(FileMetadata metadata) {
        FileRecordEntity entity = FileRecordEntity.fromDomain(metadata);
        fileRecordJpaRepository.save(entity);
    }

    public FilePage<FileMetadata> findByOwnerId(UserId userId, int pageNumber, int pageSize) {
        return null;
    }

}
