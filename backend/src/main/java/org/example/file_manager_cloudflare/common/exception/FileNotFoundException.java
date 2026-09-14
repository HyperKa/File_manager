package org.example.file_manager_cloudflare.common.exception;

import java.util.UUID;

public class FileNotFoundException extends RuntimeException {

    public FileNotFoundException(UUID id) {
        super("File not found: " + id);
    }
}
