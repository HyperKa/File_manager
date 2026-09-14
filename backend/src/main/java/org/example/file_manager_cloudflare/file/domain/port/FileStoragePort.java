package org.example.file_manager_cloudflare.file.domain.port;

import org.example.file_manager_cloudflare.common.exception.FileStorageException;
import org.example.file_manager_cloudflare.file.domain.DownloadHandle;
import org.example.file_manager_cloudflare.file.domain.StorageKey;
import org.example.file_manager_cloudflare.file.domain.StoreCommand;
import org.example.file_manager_cloudflare.file.domain.StoredFile;

import java.io.InputStream;

public interface FileStoragePort {

    StoredFile store(InputStream content, StoreCommand command) throws FileStorageException;

    DownloadHandle open(StorageKey key) throws FileStorageException;

    void delete(StorageKey key) throws FileStorageException;

    boolean exists(StorageKey key) throws FileStorageException;
}
