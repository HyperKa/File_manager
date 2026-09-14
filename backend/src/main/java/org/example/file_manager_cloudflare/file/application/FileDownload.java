package org.example.file_manager_cloudflare.file.application;

import org.example.file_manager_cloudflare.file.domain.DownloadHandle;
import org.example.file_manager_cloudflare.file.domain.FileMetadata;

public record FileDownload(
        FileMetadata metadata,
        DownloadHandle content
) {
}
