package org.example.file_manager_cloudflare.common.exception;

public class InvalidFileNameException extends RuntimeException {
    public InvalidFileNameException(String message) {
        super(message);
    }
}