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

import static org.junit.jupiter.api.Assertions.*;
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

    @Test
    @DisplayName("BE-1: Файл создаётся с первой попытки (без коллизий)")
    void shouldUploadFileOnFirstAttempt() {
        when(metadataRepo.existsById(any(FileId.class)))
                .thenReturn(false);
        when(metadataRepo.createFile(any(FileMetadata.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StorageKey storageKey = new StorageKey("files/test-key");
        StoredFile storedFile = new StoredFile(storageKey, "sha256hash", fileContent.getBytes().length);
        when(storagePort.storeFile(any(InputStream.class), any(StoreCommand.class))).thenReturn(storedFile);
        doNothing().when(metadataRepo).saveFile(any(FileMetadata.class));

        UploadFileCommand command = new UploadFileCommand(
                new ByteArrayInputStream(fileContent.getBytes()),
                "document.txt",
                "text/plain",
                fileContent.getBytes().length
        );

        FileMetadata result = uploadFileUseCase.uploadFile(command, testId);

        assertNotNull(result);
        // Доказательство итерации: метод вызван ровно 1 раз
        verify(metadataRepo, times(1)).existsById(any(FileId.class));
        verify(metadataRepo, times(1)).createFile(any(FileMetadata.class));
        verify(storagePort, times(1)).storeFile(any(InputStream.class), any(StoreCommand.class));
    }

    @Test
    @DisplayName("BE-1: Retry при коллизии FileId (успех со 2-й попытки)")
    void shouldRetryGetFileId() {
        // Настройка: 1-я попытка коллизия (true), 2-я попытка успех (false)
        when(metadataRepo.existsById(any(FileId.class)))
                .thenReturn(true)
                .thenReturn(false);

        when(metadataRepo.createFile(any(FileMetadata.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StorageKey storageKey = new StorageKey("files/address456");
        StoredFile storedFile = new StoredFile(storageKey, "sha256hash2", fileContent.getBytes().length);
        when(storagePort.storeFile(any(InputStream.class), any(StoreCommand.class)))
                .thenReturn(storedFile);
        doNothing().when(metadataRepo).saveFile(any(FileMetadata.class));

        UploadFileCommand command = new UploadFileCommand(
                new ByteArrayInputStream(fileContent.getBytes()),
                "document.txt",
                "text/plain",
                fileContent.getBytes().length
        );

        FileMetadata result = uploadFileUseCase.uploadFile(command, testId);

        assertNotNull(result);
        // Проверка, что репозиторий был опрошен РОВНО 2 раза
        verify(metadataRepo, times(2)).existsById(any(FileId.class));
        // При этом файл был создан только 1 раз (после успешной второй попытки)
        verify(metadataRepo, times(1)).createFile(any(FileMetadata.class));
        verify(storagePort, times(1)).storeFile(any(InputStream.class), any(StoreCommand.class));
    }

    @Test
    @DisplayName("BE-1: Выброс исключения после 3 неудачных попыток")
    void shouldThrowExceptionAfterThreeFailedAttempts() {
        // Given: Постоянная коллизия (3 раза)
        when(metadataRepo.existsById(any(FileId.class)))
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true);

        UploadFileCommand command = new UploadFileCommand(
                new ByteArrayInputStream(fileContent.getBytes()),
                "document.txt",
                "text/plain",
                fileContent.getBytes().length
        );

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            uploadFileUseCase.uploadFile(command, testId);
        });

        assertTrue(exception.getMessage().contains("uniqueFileId is empty") ||
                exception.getMessage().contains("Failed to generate a unique FileId"));

        verify(metadataRepo, times(3)).existsById(any(FileId.class)); // Ровно 3 проверки
        verify(metadataRepo, never()).createFile(any(FileMetadata.class)); // Ни одного создания!
        verify(storagePort, never()).storeFile(any(InputStream.class), any(StoreCommand.class)); // S3 не вызывался
    }

}
