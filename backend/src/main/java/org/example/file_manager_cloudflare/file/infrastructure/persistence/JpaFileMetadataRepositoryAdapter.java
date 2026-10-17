package org.example.file_manager_cloudflare.file.infrastructure.persistence;

import org.example.file_manager_cloudflare.file.domain.*;
import org.example.file_manager_cloudflare.file.domain.port.FileMetadataRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

        PageRequest pageRequest = PageRequest.of(
                pageNumber,
                pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<FileRecordEntity> page = fileRecordJpaRepository.findByOwnerIdAndDeletedAtIsNull(userId.value(), pageRequest);

        return new FilePage<>(
                page.getContent().stream().map(FileRecordEntity::toDomain).toList(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize()
        );
    }

}
