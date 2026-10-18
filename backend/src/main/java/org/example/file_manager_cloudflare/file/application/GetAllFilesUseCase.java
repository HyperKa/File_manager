package org.example.file_manager_cloudflare.file.application;

import org.example.file_manager_cloudflare.file.domain.FileMetadata;
import org.example.file_manager_cloudflare.file.domain.FilePage;
import org.example.file_manager_cloudflare.file.domain.UserId;
import org.example.file_manager_cloudflare.file.domain.port.FileMetadataRepository;
import org.springframework.stereotype.Service;

@Service
public class GetAllFilesUseCase {
    private final FileMetadataRepository fileMetadataRepository;

    public GetAllFilesUseCase(FileMetadataRepository fileMetadataRepository) {
        this.fileMetadataRepository = fileMetadataRepository;
    }

    public FilePage<FileMetadata> getAllFilesForOnePage(UserId userId, int pageNumber, int pageSize) {
        return fileMetadataRepository.findAllFilesByOwnerId(userId, pageNumber, pageSize);
    }
}
