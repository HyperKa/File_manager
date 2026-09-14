package org.example.file_manager_cloudflare.file.infrastructure.persistence;

import org.example.file_manager_cloudflare.file.domain.FileId;
import org.example.file_manager_cloudflare.file.domain.FileMetadata;
import org.example.file_manager_cloudflare.file.domain.FilePage;
import org.example.file_manager_cloudflare.file.domain.UserId;
import org.example.file_manager_cloudflare.file.domain.port.FileMetadataRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaFileMetadataRepositoryAdapter implements FileMetadataRepository {

    private final FileRecordJpaRepository jpaRepository;

    public JpaFileMetadataRepositoryAdapter(FileRecordJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public FileMetadata create(FileMetadata metadata) {
        return toDomain(jpaRepository.save(FileRecordEntity.from(metadata)));
    }

    @Override
    public Optional<FileMetadata> findById(FileId id) {
        return jpaRepository.findById(id.value()).map(FileRecordEntity::toDomain);
    }

    @Override
    public FilePage<FileMetadata> findByOwnerId(UserId ownerId, int pageNumber, int pageSize) {
        PageRequest pageable = PageRequest.of(
                pageNumber,
                pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        Page<FileRecordEntity> page = jpaRepository.findByOwnerIdAndDeletedAtIsNull(
                ownerId.value(),
                pageable
        );
        return new FilePage<>(
                page.getContent().stream().map(FileRecordEntity::toDomain).toList(),
                page.getTotalElements(),
                page.getNumber(),
                page.getSize(),
                page.getTotalPages()
        );
    }

    @Override
    public void save(FileMetadata metadata) {
        jpaRepository.save(FileRecordEntity.from(metadata));
    }

    private FileMetadata toDomain(FileRecordEntity entity) {
        return entity.toDomain();
    }
}
