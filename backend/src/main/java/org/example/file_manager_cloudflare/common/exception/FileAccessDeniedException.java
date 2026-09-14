package org.example.file_manager_cloudflare.common.exception;

public class FileAccessDeniedException extends RuntimeException {

    public FileAccessDeniedException() {
        super("Access to the file is denied");
    }
}
