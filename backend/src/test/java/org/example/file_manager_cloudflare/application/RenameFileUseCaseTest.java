package org.example.file_manager_cloudflare.application;

import org.example.file_manager_cloudflare.file.application.RenameFileUseCase;
import org.example.file_manager_cloudflare.file.domain.FileId;
import org.example.file_manager_cloudflare.file.domain.FileMetadata;
import org.example.file_manager_cloudflare.file.domain.FileName;
import org.example.file_manager_cloudflare.file.domain.FileMediaType;
import org.example.file_manager_cloudflare.file.domain.StorageKey;
import org.example.file_manager_cloudflare.file.domain.UserId;
import org.example.file_manager_cloudflare.file.domain.port.FileMetadataRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RenameFileUseCaseTest {

    @Mock
    private FileMetadataRepository metadataRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private RenameFileUseCase renameFileUseCase;

    private final UserId testUserId = UserId.createFromUUID(UUID.fromString("00000000-0000-0000-0000-000000000001"));

    @Test
    void execute_WhenFileExists_RenamesAndSaves() {

        byte[] textBytes = "text_from_file".getBytes();
        FileId fileId = new FileId(UUID.randomUUID());
        FileMetadata metadata = FileMetadata.pending(
                testUserId,
                new FileName("old.pdf"),
                new StorageKey("files/old"),
                new FileMediaType("application/pdf"),
                textBytes.length
        );

        when(metadataRepository.findFileById(eq(fileId))).thenReturn(Optional.of(metadata));

        FileMetadata result = renameFileUseCase.execute(fileId, "new_name.pdf");

        assertEquals("new_name.pdf", result.fileName().value());
        // теперь saveFile со сменой version нет, следовательно этот тест откорректирован:
        verify(metadataRepository, times(0)).saveFile(eq(metadata));
    }

    @Test
    void execute_WhenFileNotFound_Throws404() {
        FileId fileId = new FileId(UUID.randomUUID());
        when(metadataRepository.findFileById(eq(fileId)))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
        {
            renameFileUseCase.execute(fileId, "new_name.pdf");
        });

        verify(metadataRepository, never()).saveFile(any());
        verify(metadataRepository, times(0)).saveFile(any(FileMetadata.class));
    }
}