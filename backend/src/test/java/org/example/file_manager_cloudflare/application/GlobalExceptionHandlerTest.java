package org.example.file_manager_cloudflare.application;

import org.example.file_manager_cloudflare.common.exception.*;
import org.example.file_manager_cloudflare.file.api.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("BE-5: FileNotFoundException → 404 JSON")
    void shouldReturn404ForFileNotFound() {
        var ex = new FileNotFoundException("File with id 'abc' not found");

        ResponseEntity<ErrorResponse> response = handler.handleFileNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
        assertEquals("Not Found", response.getBody().error());
        assertEquals("File with id 'abc' not found", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    @DisplayName("BE-5: InvalidFileNameException → 400")
    void shouldReturn400ForInvalidFileName() {
        var ex = new InvalidFileNameException("File name cannot be empty");

        ResponseEntity<ErrorResponse> response = handler.handleInvalidFileName(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().status());
        assertEquals("Bad Request", response.getBody().error());
    }

    @Test
    @DisplayName("BE-5: FileStorageException → 500 (без утечки деталей)")
    void shouldReturn500ForStorageError() {
        var ex = new FileStorageException("Connection timeout to S3", new RuntimeException("timeout"));

        ResponseEntity<ErrorResponse> response = handler.handleFileStorage(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().status());
        // Критично: клиент НЕ должен видеть внутренние детали
        assertFalse(response.getBody().message().contains("timeout"));
        assertEquals("Storage service unavailable", response.getBody().message());
    }

    @Test
    @DisplayName("BE-5: Неожиданная ошибка → 500 без стектрейса")
    void shouldReturn500ForUnexpectedError() {
        var ex = new NullPointerException("internal NPE");

        ResponseEntity<ErrorResponse> response = handler.handleGeneric(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().message());
        assertFalse(response.getBody().message().contains("NPE"));
    }
}