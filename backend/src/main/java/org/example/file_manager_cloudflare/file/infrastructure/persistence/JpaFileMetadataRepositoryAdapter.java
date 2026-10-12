package org.example.file_manager_cloudflare.file.infrastructure.persistence;

import org.example.file_manager_cloudflare.file.domain.*;
import org.example.file_manager_cloudflare.file.domain.port.FileMetadataRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaFileMetadataRepositoryAdapter implements FileMetadataRepository {
    private final FileRecordJpaRepository fileRecordJpaRepository;

    public JpaFileMetadataRepositoryAdapter(FileRecordJpaRepository fileRecordJpaRepository) {
        this.fileRecordJpaRepository = fileRecordJpaRepository;
    }

    @Override
    public FileMetadata createFile(FileMetadata metadata) {
        FileRecordEntity entity = FileRecordEntity.fromDomain(metadata);
        return fileRecordJpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<FileMetadata> findFileById(FileId id) {
        return fileRecordJpaRepository.findById(id.value()).map(FileRecordEntity::toDomain);
    }

    @Override
    public void saveFile(FileMetadata metadata) {
        FileRecordEntity entity = FileRecordEntity.fromDomain(metadata);
        fileRecordJpaRepository.save(entity);
    }

    @Override
    public FilePage<FileMetadata> findAllFilesByOwnerId(UserId userId, int pageNumber, int pageSize) {
        return null;
    }

}
