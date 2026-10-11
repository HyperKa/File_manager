package org.example.file_manager_cloudflare.file.application;

import org.example.file_manager_cloudflare.common.exception.FileStorageException;
import org.example.file_manager_cloudflare.common.exception.FileNotFoundException;
import org.example.file_manager_cloudflare.common.exception.InvalidFileException;
import org.example.file_manager_cloudflare.file.domain.FileId;
import org.example.file_manager_cloudflare.file.domain.FileMediaType;
import org.example.file_manager_cloudflare.file.domain.FileMetadata;
import org.example.file_manager_cloudflare.file.domain.FileName;
import org.example.file_manager_cloudflare.file.domain.StorageKey;
import org.example.file_manager_cloudflare.file.domain.StoreCommand;
import org.example.file_manager_cloudflare.file.domain.StoredFile;
import org.example.file_manager_cloudflare.file.domain.UserId;
import org.example.file_manager_cloudflare.file.domain.port.FileMetadataRepository;
import org.example.file_manager_cloudflare.file.domain.port.FileStoragePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;

@Service
public class UploadFileUseCase {

    private final FileMetadataRepository metadataRepository;
    private final FileStoragePort storagePort;
    private final TransactionTemplate transactionTemplate;
    private final UploadProperties properties;

    public UploadFileUseCase(
            FileMetadataRepository metadataRepository,
            FileStoragePort storagePort,
            TransactionTemplate transactionTemplate,
            UploadProperties properties
    ) {
        this.metadataRepository = metadataRepository;
        this.storagePort = storagePort;
        this.transactionTemplate = transactionTemplate;
        this.properties = properties;
    }

    public FileMetadata upload(UploadFileCommand command, UserId ownerId) {
        validate(command);

        FileId id = FileId.generateNewRandomUUID();
        FileName fileName = new FileName(command.fileName());
        FileMediaType mediaType = FileMediaType.of(command.mediaType());
        StorageKey storageKey = new StorageKey("files/" + id);

        FileMetadata pending = FileMetadata.pending(
                ownerId,
                fileName,
                storageKey,
                mediaType,
                command.sizeBytes()
        );

        FileMetadata created = createPending(pending);
        try {
            StoredUpload storedUpload = storeContent(command, storageKey, mediaType);
            return complete(
                    created.id(),
                    storedUpload.storedFile(),
                    HexFormat.of().formatHex(storedUpload.sha256())
            );
        } catch (Exception exception) {
            failQuietly(created.id(), exception);
            if (exception instanceof FileStorageException storageException) {
                throw storageException;
            }
            if (exception instanceof InvalidFileException invalidFileException) {
                throw invalidFileException;
            }
            throw new FileStorageException("Unable to upload file", exception);
        }
    }

    private void validate(UploadFileCommand command) {
        if (command.sizeBytes() == 0) {
            throw new InvalidFileException("File must not be empty");
        }
        if (command.sizeBytes() > properties.getMaxFileSizeBytes()) {
            throw new InvalidFileException("File is too large");
        }
        if (command.fileName().getBytes(StandardCharsets.UTF_8).length
                > properties.getMaxFileNameBytes()) {
            throw new InvalidFileException("File name is too long");
        }
    }

    private FileMetadata createPending(FileMetadata metadata) {
        return transactionTemplate.execute(status -> metadataRepository.create(metadata));
    }

    private StoredUpload storeContent(UploadFileCommand command, StorageKey storageKey, FileMediaType mediaType) throws IOException {
        DigestStream digestStream = new DigestStream(command.content());
        try {
            StoredFile storedFile = storagePort.store(
                    digestStream,
                    new StoreCommand(storageKey, mediaType, command.sizeBytes())
            );
            return new StoredUpload(storedFile, digestStream.digest());
        } finally {
            digestStream.close();
        }
    }

    private FileMetadata complete(FileId id, StoredFile storedFile, String checksum) {

        return transactionTemplate.execute(status -> {

            FileMetadata current = metadataRepository.findById(id)
                    .orElseThrow(() -> new FileNotFoundException(id.value()));

            current.markReady(storedFile.eTag(), checksum);

            metadataRepository.save(current);


            return current;
        });
    }

    private void failQuietly(FileId id, Exception exception) {
        try {
            transactionTemplate.executeWithoutResult(status -> {
                metadataRepository.findById(id).ifPresent(metadata -> {
                    metadata.markFailed();
                    metadataRepository.save(metadata);
                });
            });
        } catch (RuntimeException ignored) {
            // The reconciler can handle a failed metadata update later.
        }
    }

    private record StoredUpload(
            StoredFile storedFile,
            byte[] sha256
    ) {
    }

    private static final class DigestStream extends InputStream {

        private final InputStream delegate;
        private final MessageDigest digest;

        private DigestStream(InputStream delegate) {
            try {
                this.delegate = delegate;
                this.digest = MessageDigest.getInstance("SHA-256");
            } catch (GeneralSecurityException exception) {
                throw new IllegalStateException("SHA-256 is not available", exception);
            }
        }

        @Override
        public int read() throws IOException {
            int value = delegate.read();
            if (value >= 0) {
                digest.update((byte) value);
            }
            return value;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int count = delegate.read(b, off, len);
            if (count > 0) {
                digest.update(b, off, count);
            }
            return count;
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }

        private byte[] digest() {
            return digest.digest();
        }
    }
}
