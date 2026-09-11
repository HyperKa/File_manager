package org.example.file_manager_cloudflare;

import org.example.file_manager_cloudflare.dto.FileResponse;
import org.example.file_manager_cloudflare.service.FileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class IntegrationTest {

    @Autowired
    private FileService fileService;

    @Test
    void testFullUploadCycle() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test-hello.txt",
                "text/plain",
                "Hello, Java 25 and Neon!".getBytes()
        );

        FileResponse response = fileService.uploadFile(mockFile);

        assertNotNull(response.id(), "ID из базы Neon должен быть получен");
        System.out.println(">>> УСПЕХ! Файл сохранен в базе с ID: " + response.id());

        assertTrue(response.fileName().contains("test-hello.txt"));
        System.out.println(">>> Путь для скачивания: " + response.downloadUrl());
    }
}