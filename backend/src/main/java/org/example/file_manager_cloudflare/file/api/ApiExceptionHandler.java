package org.example.file_manager_cloudflare.file.api;

import org.example.file_manager_cloudflare.common.exception.FileAccessDeniedException;
import org.example.file_manager_cloudflare.common.exception.FileNotFoundException;
import org.example.file_manager_cloudflare.common.exception.FileStateException;
import org.example.file_manager_cloudflare.common.exception.FileStorageException;
import org.example.file_manager_cloudflare.common.exception.InvalidFileException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.net.URI;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InvalidFileException.class)
    ResponseEntity<ProblemDetail> invalidFile(InvalidFileException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid file", exception.getMessage());
    }

    @ExceptionHandler(FileNotFoundException.class)
    ResponseEntity<ProblemDetail> fileNotFound(FileNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "File not found", exception.getMessage());
    }

    @ExceptionHandler(FileAccessDeniedException.class)
    ResponseEntity<ProblemDetail> accessDenied(FileAccessDeniedException exception) {
        return problem(HttpStatus.FORBIDDEN, "Access denied", exception.getMessage());
    }

    @ExceptionHandler(FileStateException.class)
    ResponseEntity<ProblemDetail> invalidState(FileStateException exception) {
        return problem(HttpStatus.CONFLICT, "Invalid file state", exception.getMessage());
    }

    @ExceptionHandler(FileStorageException.class)
    ResponseEntity<ProblemDetail> storageUnavailable(FileStorageException exception) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "Storage unavailable", "Object storage is temporarily unavailable");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ProblemDetail> tooLarge(MaxUploadSizeExceededException exception) {
        return problem(HttpStatus.PAYLOAD_TOO_LARGE, "File is too large", exception.getMessage());
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            MissingServletRequestPartException.class,
            HttpMessageNotReadableException.class
    })
    ResponseEntity<ProblemDetail> invalidRequest(Exception exception) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid request", exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> unexpected(Exception exception) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", "Unexpected server error");
    }

    private ResponseEntity<ProblemDetail> problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create("about:blank"));
        return ResponseEntity.status(status).body(problem);
    }
}
