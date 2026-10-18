package org.example.file_manager_cloudflare.file.api;

import org.example.file_manager_cloudflare.file.application.GetAllFilesUseCase;
import org.example.file_manager_cloudflare.file.application.UploadFileCommand;
import org.example.file_manager_cloudflare.file.application.UploadFileUseCase;
import org.example.file_manager_cloudflare.file.domain.FileMetadata;
import org.example.file_manager_cloudflare.file.domain.FilePage;
import org.example.file_manager_cloudflare.file.domain.UserId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {
    private final UploadFileUseCase uploadFileUseCase;
    private static final UserId testId = UserId.createFromUUID(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private final GetAllFilesUseCase getAllFilesUseCase;

    public FileController(UploadFileUseCase uploadFileUseCase, GetAllFilesUseCase getAllFilesUseCase) {
        this.uploadFileUseCase = uploadFileUseCase;
        this.getAllFilesUseCase = getAllFilesUseCase;
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
}
