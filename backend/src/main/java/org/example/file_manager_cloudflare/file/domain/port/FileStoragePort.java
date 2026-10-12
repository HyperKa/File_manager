package org.example.file_manager_cloudflare.file.domain.port;

import org.example.file_manager_cloudflare.file.domain.StoreCommand;
import org.example.file_manager_cloudflare.file.domain.StoredFile;

import java.io.InputStream;

public interface FileStoragePort {
    StoredFile storeFile(InputStream content, StoreCommand command);
}
