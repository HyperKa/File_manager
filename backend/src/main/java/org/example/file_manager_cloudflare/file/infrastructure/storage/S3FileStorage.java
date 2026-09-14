package org.example.file_manager_cloudflare.file.infrastructure.storage;

import org.example.file_manager_cloudflare.common.exception.FileStorageException;
import org.example.file_manager_cloudflare.file.domain.DownloadHandle;
import org.example.file_manager_cloudflare.file.domain.StoreCommand;
import org.example.file_manager_cloudflare.file.domain.StorageKey;
import org.example.file_manager_cloudflare.file.domain.StoredFile;
import org.example.file_manager_cloudflare.file.domain.port.FileStoragePort;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.InputStream;

public class S3FileStorage implements FileStoragePort {

    private final S3Client s3Client;
    private final String bucket;

    public S3FileStorage(S3Client s3Client, String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    @Override
    public StoredFile store(InputStream content, StoreCommand command) throws FileStorageException {
        try {
            PutObjectResponse response = s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(command.key().value())
                            .contentType(command.mediaType().value())
                            .contentLength(command.sizeBytes())
                            .build(),
                    RequestBody.fromInputStream(content, command.sizeBytes())
            );
            return new StoredFile(command.key(), response.eTag(), command.sizeBytes());
        } catch (SdkClientException exception) {
            throw new FileStorageException("Unable to store object in S3-compatible storage", exception);
        }
    }

    @Override
    public DownloadHandle open(StorageKey key) throws FileStorageException {
        try {
            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(
                    GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(key.value())
                            .build()
            );
            GetObjectResponse objectResponse = response.response();
            return new S3DownloadHandle(
                    response,
                    objectResponse.contentType(),
                    objectResponse.contentLength() == null ? -1L : objectResponse.contentLength(),
                    objectResponse.eTag()
            );
        } catch (SdkClientException exception) {
            throw new FileStorageException("Unable to open object from S3-compatible storage", exception);
        }
    }

    @Override
    public void delete(StorageKey key) throws FileStorageException {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key.value())
                    .build());
        } catch (SdkClientException exception) {
            throw new FileStorageException("Unable to delete object from S3-compatible storage", exception);
        }
    }

    @Override
    public boolean exists(StorageKey key) throws FileStorageException {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key.value())
                    .build());
            return true;
        } catch (NoSuchKeyException exception) {
            return false;
        } catch (SdkClientException exception) {
            throw new FileStorageException("Unable to check object in S3-compatible storage", exception);
        }
    }

    private static final class S3DownloadHandle implements DownloadHandle {

        private final ResponseInputStream<GetObjectResponse> response;
        private final String contentType;
        private final long sizeBytes;
        private final String eTag;

        private S3DownloadHandle(
                ResponseInputStream<GetObjectResponse> response,
                String contentType,
                long sizeBytes,
                String eTag
        ) {
            this.response = response;
            this.contentType = contentType;
            this.sizeBytes = sizeBytes;
            this.eTag = eTag;
        }

        @Override
        public InputStream inputStream() {
            return response;
        }

        @Override
        public String contentType() {
            return contentType;
        }

        @Override
        public long sizeBytes() {
            return sizeBytes;
        }

        @Override
        public String eTag() {
            return eTag;
        }

        @Override
        public void close() throws Exception {
            response.close();
        }
    }
}
