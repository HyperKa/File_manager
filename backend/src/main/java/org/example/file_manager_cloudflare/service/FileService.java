package org.example.file_manager_cloudflare.service;

import org.example.file_manager_cloudflare.dto.FileResponse;
import org.example.file_manager_cloudflare.entity.FileRecord;
import org.example.file_manager_cloudflare.repo.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    private final FileRepository repository;
    private final S3Client s3Client; // Теперь внедряется через конструктор благодаря Lombok

    private final String bucketName = "my-files";

    public FileService(FileRepository repository, S3Client s3Client) {
        this.repository = repository;
        this.s3Client = s3Client;
    }

    public FileResponse uploadFile(MultipartFile file) throws IOException {
        String key = UUID.randomUUID() + "_" + file.getOriginalFilename();

        // 1. Загрузка в MinIO
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        // 2. Подготовка сущности для базы Neon
        FileRecord entity = new FileRecord(
                file.getOriginalFilename(),
                file.getContentType(),
                key,
                (double) file.getSize()
        );

        // 3. Сохранение в базу (ВАЖНО: присваиваем результат переменной)
        FileRecord savedEntity = repository.save(entity);

        // 4. Возвращаем DTO
        return new FileResponse(
                savedEntity.getId(),
                savedEntity.getFileName(),
                "/api/files/download/" + savedEntity.getId(),
                savedEntity.getFileType(),
                formatSize(savedEntity.getFileSize())
        );
    }

    public List<FileResponse> showFiles() {
        return repository.findAll().stream()
                .map(entity -> new FileResponse(
                        entity.getId(),
                        entity.getFileName(),
                        "/api/files/download/" + entity.getId(),
                        entity.getFileType(),
                        formatSize(entity.getFileSize())
                ))
                .toList();
    }

    public void deleteFile(Long id) {
        FileRecord entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Файл не найден с ID: " + id));

        // 1. Удаляем из MinIO
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(entity.getS3Key())
                .build());

        // 2. Удаляем из базы Neon
        repository.deleteById(id);
    }

    private String formatSize(double size) {
        return String.format("%.2f KB", size / 1024);
    }
}
