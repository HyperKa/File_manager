package org.example.file_manager_cloudflare.application;

import org.example.file_manager_cloudflare.file.application.DownloadFileUseCase;
import org.example.file_manager_cloudflare.file.domain.*;
import org.example.file_manager_cloudflare.file.domain.port.FileMetadataRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DownloadFileUseCaseTest {

    @Mock
    private FileMetadataRepository metadataRepository;

    @InjectMocks
    private DownloadFileUseCase downloadFileUseCase;

    private final UserId testUserId = UserId.createFromUUID(
            UUID.fromString("00000000-0000-0000-0000-000000000001")
    );

    @Test
    public void execute_WhenFileExists_ReturnsFileMetadata() {
        byte[] fileContent = "fake pdf content".getBytes();

        FileId fileId = FileId.generateNewRandomUUID();
        FileMetadata metadata = FileMetadata.pending(
                testUserId,
                new FileName("test.pdf"),
                new StorageKey("files/" + fileId.value()),
                new FileMediaType("application/pdf"),
                fileContent.length
        );

        metadata.markReady("etag", null);

        when(metadataRepository.findFileById(eq(fileId)))
                .thenReturn(Optional.of(metadata));

        Optional<FileMetadata> result = downloadFileUseCase.execute(fileId);

        assertTrue(result.isPresent());
        assertEquals(metadata, result.get());
        verify(metadataRepository, times(1)).findFileById(eq(fileId));

    }

    @Test
    void execute_WhenFileNotFound_ReturnsEmptyOptional() {
        FileId fileId = new FileId(UUID.randomUUID());
        when(metadataRepository.findFileById(eq(fileId)))
                .thenReturn(Optional.empty());

        Optional<FileMetadata> result = downloadFileUseCase.execute(fileId);

        assertTrue(result.isEmpty());
        verify(metadataRepository, times(1)).findFileById(eq(fileId));
    }
}
