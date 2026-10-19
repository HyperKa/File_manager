package org.example.file_manager_cloudflare.file.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RenameFileRequest(
        @NotNull
        @Size(min = 1, max = 255, message = "<RenameFileRequest>: Name must be between 1 and 255 characters")
        String newName)
{}
