package org.example.file_manager_cloudflare.controller;

import org.example.file_manager_cloudflare.dto.FileResponse;
import org.example.file_manager_cloudflare.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@CrossOrigin("*") // Чтобы React/Vue мог делать запросы
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    // Загрузка файла
    @PostMapping("/upload")
    public ResponseEntity<FileResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            FileResponse response = fileService.uploadFile(file);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<FileResponse>> getAllFiles() {
        return ResponseEntity.ok(fileService.showFiles());
    }

    // Удаление файла по ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long id) {
        try {
            fileService.deleteFile(id);
            return ResponseEntity.noContent().build(); // Возвращаем статус 204 (успешно, без контента)
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build(); // Если файл не найден в БД
        }
    }
}
