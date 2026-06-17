package org.example.file_manager_cloudflare.dto;

public record FileResponce(
        Long id,
        String fileName,
        String downloadUrl,
        String fileType,
        double fileSize
) {

}
