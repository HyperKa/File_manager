package org.example.file_manager_cloudflare.file.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.example.file_manager_cloudflare.file.application.*;
import org.example.file_manager_cloudflare.file.domain.DownloadHandle;
import org.example.file_manager_cloudflare.file.domain.FileId;
import org.example.file_manager_cloudflare.file.domain.FileMetadata;
import org.example.file_manager_cloudflare.file.domain.UserId;
import org.example.file_manager_cloudflare.security.CurrentUserService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final UploadFileUseCase uploadFileUseCase;
    private final DownloadFileUseCase downloadFileUseCase;
    private final DeleteFileUseCase deleteFileUseCase;
    private final ListFilesUseCase listFilesUseCase;
    private final CurrentUserService currentUserService;
    private final RenameFileUseCase renameFileUseCase;

    public FileController(
            UploadFileUseCase uploadFileUseCase,
            DownloadFileUseCase downloadFileUseCase,
            DeleteFileUseCase deleteFileUseCase,
            ListFilesUseCase listFilesUseCase,
            CurrentUserService currentUserService,
            RenameFileUseCase renameFileUseCase
    ) {
        this.uploadFileUseCase = uploadFileUseCase;
        this.downloadFileUseCase = downloadFileUseCase;
        this.deleteFileUseCase = deleteFileUseCase;
        this.listFilesUseCase = listFilesUseCase;
        this.currentUserService = currentUserService;
        this.renameFileUseCase = renameFileUseCase;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResponse> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        UserId ownerId = currentUserService.currentUserId();
        UploadFileCommand command = new UploadFileCommand(
                file.getInputStream(),
                file.getOriginalFilename() == null ? "unnamed" : file.getOriginalFilename(),
                file.getContentType(),
                file.getSize()
        );

        FileMetadata metadata = uploadFileUseCase.upload(command, ownerId);
        URI location = URI.create("/api/v1/files/" + metadata.id());
        return ResponseEntity.created(location).body(FileResponse.from(metadata));
    }

    @GetMapping
    public ResponseEntity<FilePageResponse> listFiles(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size
    ) {
        UserId ownerId = currentUserService.currentUserId();
        return ResponseEntity.ok(FilePageResponse.from(listFilesUseCase.list(ownerId, page, size)));
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<StreamingResponseBody> downloadFile(@PathVariable UUID id) throws IOException {
        UserId ownerId = currentUserService.currentUserId();
        FileDownload download = downloadFileUseCase.open(FileId.from(id), ownerId);
        FileMetadata metadata = download.metadata();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.mediaType().value()))
                .contentLength(download.content().sizeBytes() >= 0 ? download.content().sizeBytes() : 0L)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(metadata.originalName().value(), StandardCharsets.UTF_8)
                                .build()
                                .toString()
                )
                .header(HttpHeaders.ETAG, toQuotedEtag(download.content().eTag()))
                .body(new StreamingResponseBody() {
                    @Override
                    public void writeTo(OutputStream outputStream) throws IOException {
                        try (DownloadHandle handle = download.content()) {
                            handle.inputStream().transferTo(outputStream);
                        } catch (Exception exception) {
                            if (exception instanceof IOException ioException) {
                                throw ioException;
                            }
                            if (exception instanceof RuntimeException runtimeException) {
                                throw runtimeException;
                            }
                            throw new IOException(exception);
                        }
                    }
                });
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable UUID id) {
        deleteFileUseCase.delete(FileId.from(id), currentUserService.currentUserId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FileResponse> renameFile(
            @PathVariable UUID id,
            @RequestBody @jakarta.validation.Valid RenameFileRequest request
    ) {
        UserId ownerId = currentUserService.currentUserId();
        FileMetadata updated = renameFileUseCase.rename(FileId.from(id), ownerId, request.newName());
        return ResponseEntity.ok(FileResponse.from(updated));
    }

    private String toQuotedEtag(String eTag) {
        if (eTag == null || eTag.isBlank()) {
            return "";
        }
        return eTag.startsWith("\"") ? eTag : "\"" + eTag + "\"";
    }
}
