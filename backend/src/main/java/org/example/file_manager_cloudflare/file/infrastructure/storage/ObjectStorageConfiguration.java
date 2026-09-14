package org.example.file_manager_cloudflare.file.infrastructure.storage;

import org.example.file_manager_cloudflare.file.domain.port.FileStoragePort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class ObjectStorageConfiguration {

    @Bean
    S3Client s3Client(StorageProperties properties) {
        if (properties.getProvider() != StorageProvider.S3) {
            throw new IllegalStateException("Only S3-compatible storage is currently supported");
        }

        StorageProperties.S3Properties s3 = properties.getS3();
        return S3Client.builder()
                .endpointOverride(URI.create(s3.getEndpoint()))
                .region(Region.of(s3.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(s3.getAccessKey(), s3.getSecretKey())
                ))
                .forcePathStyle(s3.isPathStyle())
                .build();
    }

    @Bean
    FileStoragePort fileStoragePort(S3Client s3Client, StorageProperties properties) {
        return new S3FileStorage(s3Client, properties.getS3().getBucket());
    }
}
