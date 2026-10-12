package org.example.file_manager_cloudflare.file.infrastructure.storage;

import org.example.file_manager_cloudflare.file.domain.StoreCommand;
import org.example.file_manager_cloudflare.file.domain.StoredFile;
import org.example.file_manager_cloudflare.file.domain.port.FileStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.InputStream;

@Service
public class S3FileStorageAdapter implements FileStoragePort {
    public final String bucketName;
    public final S3Client s3Client;

    public S3FileStorageAdapter(@Value("${app.storage.s3.bucket:my-files}") String bucketName, S3Client s3Client) {
        this.bucketName = bucketName;
        this.s3Client = s3Client;
    }

    @Override
    public StoredFile storeFile(InputStream content, StoreCommand command) {
        try {
            // создание шаблона для отправки файла на S3
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(command.key().value())
                    .contentType(command.mediaType().value())
                    .contentLength(command.sizeBytes())
                    .build();

            // отправка HTTPS запроса по шаблону в S3 хранилище
            PutObjectResponse response = s3Client.putObject(
                    putRequest,
                    RequestBody.fromInputStream(content, command.sizeBytes())
            );

            return new StoredFile(command.key(), response.eTag(), command.sizeBytes());
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при отправке файла в S3-хранилище", e);
        }
    }


}
