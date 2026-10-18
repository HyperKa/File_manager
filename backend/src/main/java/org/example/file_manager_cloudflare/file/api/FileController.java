package org.example.file_manager_cloudflare.file.api;

import org.example.file_manager_cloudflare.file.application.DownloadFileUseCase;
import org.example.file_manager_cloudflare.file.application.GetAllFilesUseCase;
import org.example.file_manager_cloudflare.file.application.UploadFileCommand;
import org.example.file_manager_cloudflare.file.application.UploadFileUseCase;
import org.example.file_manager_cloudflare.file.domain.FileId;
import org.example.file_manager_cloudflare.file.domain.FileMetadata;
import org.example.file_manager_cloudflare.file.domain.FilePage;
import org.example.file_manager_cloudflare.file.domain.UserId;
import org.example.file_manager_cloudflare.file.domain.port.FileStoragePort;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {
    private final UploadFileUseCase uploadFileUseCase;
    private static final UserId testId = UserId.createFromUUID(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private final GetAllFilesUseCase getAllFilesUseCase;
    private final DownloadFileUseCase downloadFileUseCase;
    private final FileStoragePort fileStoragePort;

    public FileController(UploadFileUseCase uploadFileUseCase, GetAllFilesUseCase getAllFilesUseCase, DownloadFileUseCase downloadFileUseCase, FileStoragePort fileStoragePort) {
        this.uploadFileUseCase = uploadFileUseCase;
        this.getAllFilesUseCase = getAllFilesUseCase;
        this.downloadFileUseCase = downloadFileUseCase;
        this.fileStoragePort = fileStoragePort;
    }

    /**
     *
     * @param file
     *        метод file.getInputStream() из сервера Tomcat уже буферизует файл внутри, и принимает FileInputStream:
     *        new BufferedInputStream(new FileInputStream(file))
     * @return
     * @throws IOException - стандартное проверяемое исключение для getInputStream()
     */

    @PostMapping
    public ResponseEntity<FileResponse> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {

        // валидация пустого файла:
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        UploadFileCommand command = new UploadFileCommand(
                file.getInputStream(),
                file.getOriginalFilename() == null ? "noname" : file.getOriginalFilename(),
                file.getContentType() == null ? "application/octet-stream" : file.getContentType(),
                file.getSize()
        );

        FileMetadata fileMetadata = uploadFileUseCase.uploadFile(command, testId);
        URI location = URI.create("/api/v1/files/" + fileMetadata.id().value());
        return ResponseEntity.created(location).body(FileResponse.from(fileMetadata));
    }

    @GetMapping
    public ResponseEntity<FilePageResponse> getFile(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "50") int size) {
        FilePage<FileMetadata> domainPage = getAllFilesUseCase.getAllFilesForOnePage(testId, page, size);

        List<FileResponse> content = domainPage.content().stream()
                .map(FileResponse::from).toList();

        FilePageResponse response = new FilePageResponse(
                content,
                domainPage.totalElements(),
                domainPage.totalPages());

        return ResponseEntity.ok(response);
    }

    /**
     *
     * @param id
     * @return HTTP Response вида:
     *
     * HTTP/1.1 200 OK
     * Content-Type: application/pdf
     * Content-Disposition: attachment; filename="%D0%BC%D0%BE%D0%B9_%D1%84%D0%B0%D0%B9%D0%BB.pdf"
     * Content-Length: 102456
     *
     * [ЧИСТЫЕ БАЙТЫ ФАЙЛА: 0x25 0x50 0x44 0x46 ... ]
     *
     * InputStreamResponse - обертка над InputStream, позволяющее Spring не превращать байтовый поток в
     * JSON автоматически, а оставлять его в текущем виде, не изменять
     */
    @GetMapping("/{id}/content")
    public ResponseEntity<InputStreamResource> getFile(@PathVariable("id") UUID id) {
        FileId fileId = new FileId(id);
        FileMetadata metadata = downloadFileUseCase.execute(fileId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "File not found"
                ));

        InputStream inputStream = fileStoragePort.getFileContent(metadata.storageKey());
        InputStreamResource resource = new InputStreamResource(inputStream);

        // Кодировка имени файла для парсинга названия файла на вебе
        String encodedFileName = URLEncoder.encode(metadata.fileName().value(), StandardCharsets.UTF_8)
                .replace("+", "%20");               // URLEncoder кодирует пробел как '+', а для HTTP нужен '%20'

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.mediaType().value()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                .contentLength(metadata.sizeBytes())
                .body(resource);
    }
}
