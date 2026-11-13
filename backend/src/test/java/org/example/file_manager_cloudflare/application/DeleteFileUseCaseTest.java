package org.example.file_manager_cloudflare.application;

import org.example.file_manager_cloudflare.file.application.DeleteFileUseCase;
import org.example.file_manager_cloudflare.file.domain.*;
import org.example.file_manager_cloudflare.file.domain.port.FileMetadataRepository;
import org.example.file_manager_cloudflare.file.domain.port.FileStoragePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteFileUseCaseTest {

    @Mock
    private FileMetadataRepository metadataRepository;

    @Mock
    private FileStoragePort storagePort;

    @InjectMocks
    private DeleteFileUseCase deleteFileUseCase;

    private final UserId testUserId = UserId.createFromUUID(
            UUID.fromString("00000000-0000-0000-0000-000000000001")
    );

    @Test
    void execute_WhenFileExists_DeletesFromS3AndDatabase() {
        FileId fileId = new FileId(UUID.randomUUID());
        byte[] fileContent = "test content".getBytes();

        FileMetadata metadata = FileMetadata.pending(
                testUserId,
                new FileName("test.pdf"),
                new StorageKey("files/" + fileId.value()),
                new FileMediaType("application/pdf"),
                fileContent.length
        );

        when(metadataRepository.findFileById(eq(fileId))).thenReturn(Optional.of(metadata));

        deleteFileUseCase.execute(fileId);

        verify(storagePort, times(1)).deleteFile(eq(metadata.storageKey()));
        verify(metadataRepository, times(1)).deleteFile(eq(fileId));
    }

    @Test
    void execute_WhenFileNotFound_Throws404() {
        // Arrange
        FileId fileId = new FileId(UUID.randomUUID());
        when(metadataRepository.findFileById(eq(fileId))).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            deleteFileUseCase.execute(fileId);
        });

        verify(storagePort, never()).deleteFile(any());
        verify(metadataRepository, never()).deleteFile(any());
    }
}