package org.example.file_manager_cloudflare.file.infrastructure.persistence;

import org.example.file_manager_cloudflare.file.domain.*;
import org.example.file_manager_cloudflare.file.domain.port.FileMetadataRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class JpaFileMetadataRepositoryAdapter implements FileMetadataRepository {
    private final FileRecordJpaRepository jpaRepository;

    public JpaFileMetadataRepositoryAdapter(FileRecordJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public FileMetadata createFile(FileMetadata metadata) {
        FileRecordEntity entity = FileRecordEntity.fromDomain(metadata);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<FileMetadata> findFileById(FileId id) {
        return jpaRepository.findById(id.value()).map(FileRecordEntity::toDomain);
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
        // Новый fileRecordEntity сохраняется в бд, меняется поле версии при изменении
        FileRecordEntity entity = FileRecordEntity.fromDomain(metadata);
        jpaRepository.save(entity);
    }

    @Override
    public FilePage<FileMetadata> findAllFilesByOwnerId(UserId userId, int pageNumber, int pageSize) {

        PageRequest pageRequest = PageRequest.of(
                pageNumber,
                pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<FileRecordEntity> page = jpaRepository.findByOwnerIdAndDeletedAtIsNull(userId.value(), pageRequest);

        return new FilePage<>(
                page.getContent().stream().map(FileRecordEntity::toDomain).toList(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize()
        );
    }

    @Override
    public void deleteFile(FileId id) {
        jpaRepository.deleteById(id.value());
    }

    @Override
    @Transactional
    public void updateFileName(FileId fileId, FileName newName) {
        FileRecordEntity entity = jpaRepository.findById(fileId.value())
                .orElseThrow(() -> new RuntimeException("File not found"));
        entity.setFileName(newName.value());
        jpaRepository.save(entity);
    }


}
