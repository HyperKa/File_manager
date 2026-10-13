package org.example.file_manager_cloudflare.application;

import org.example.file_manager_cloudflare.file.application.UploadFileCommand;
import org.example.file_manager_cloudflare.file.application.UploadFileUseCase;
import org.example.file_manager_cloudflare.file.domain.*;
import org.example.file_manager_cloudflare.file.domain.port.FileMetadataRepository;
import org.example.file_manager_cloudflare.file.domain.port.FileStoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UploadFileUseCaseTest {

    @Mock
    private FileMetadataRepository metadataRepo;

    @Mock
    private FileStoragePort storagePort;

    @Mock
    private PlatformTransactionManager transactionManager;

    private UploadFileUseCase uploadFileUseCase;

    private final UserId testId = UserId.createFromUUID(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private final String fileContent = "текстовый документ на кириллице";

    // аннотация, вызывающая метод setUp в реализации Mockito после конструктора и заполнения мок-полей
    @BeforeEach
    public void setUp() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        uploadFileUseCase = new UploadFileUseCase(metadataRepo, storagePort, transactionTemplate);
    }

    @Test
    @DisplayName("Успешная загрузка: черновик создается, байты летят в S3, статус меняется на READY")
    void shouldUploadFileSuccessfully() {


        UploadFileCommand command = new UploadFileCommand(
                new ByteArrayInputStream(fileContent.getBytes()),
                "document.txt",
                "text/plain",
                fileContent.getBytes().length
        );

        when(metadataRepo.createFile(any(FileMetadata.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(storagePort.storeFile(any(InputStream.class), any(StoreCommand.class)))
                .thenReturn(new StoredFile(new StorageKey("files/all_UTF8_address_here"), "etag-12345", fileContent.getBytes().length));

        // т.к. saveFile это void метод, он замокирован автоматически и возвращает doNothing()

        FileMetadata result = uploadFileUseCase.uploadFile(command, testId);

        assertNotNull(result);
        assertEquals(FileStatus.READY, result.status(), "Статус должен стать READY");
        assertEquals("document.txt", result.fileName().value(), "Имя файла должно быть document.txt");

        verify(metadataRepo, times(1)).createFile(any(FileMetadata.class));
        verify(storagePort, times(1)).storeFile(any(InputStream.class), any(StoreCommand.class));
        verify(metadataRepo, times(1)).saveFile(any(FileMetadata.class));
    }


    @Test
    @DisplayName("Загрузка не удалась - файл в БД помечен как FAILED")
    void shouldSetStatusFailedUploadFile() {

        UploadFileCommand command = new UploadFileCommand(
                new ByteArrayInputStream(fileContent.getBytes()),
                "document.txt",
                "text/plain",
                fileContent.getBytes().length
        );

        when(metadataRepo.createFile(any(FileMetadata.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Ошибка при сохранении файла в S3
        when(storagePort.storeFile(any(InputStream.class), any(StoreCommand.class)))
                .thenThrow(new RuntimeException("Ошибка при загрузке файла в S3"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                uploadFileUseCase.uploadFile(command, testId)
        );

        // чек текста сообщения:
        assertEquals("Ошибка при загрузке файла в S3", exception.getMessage());


        assertNotNull(exception);
        assertEquals(RuntimeException.class, exception.getClass(), "Класс должен быть RuntimeException");

        verify(metadataRepo, times(1)).createFile(any(FileMetadata.class));
        verify(storagePort, times(1)).storeFile(any(InputStream.class), any(StoreCommand.class));

        // перехват значения статуса при сохранении metadata объекта
        verify(metadataRepo, times(1)).saveFile(argThat(metadata ->
                metadata.status() == FileStatus.FAILED));
    }
}
