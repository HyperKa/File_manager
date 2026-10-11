package org.example.file_manager_cloudflare.file.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenameFileRequest(
        @NotBlank(message = "File name must not be blank")
        @Size(max = 255, message = "File name must not exceed 255 characters")
        String newName
) {
}