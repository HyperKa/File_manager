package org.example.file_manager_cloudflare.file.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.upload")
public class UploadProperties {

    private long maxFileSizeBytes = 104_857_600L;
    // вынести в ГЛОБАЛЬНЫЕ ПЕРЕМЕННЫЕ, в domain.FileName уже есть
    private int maxFileNameBytes = 255;

    public long getMaxFileSizeBytes() {
        return maxFileSizeBytes;
    }

    public void setMaxFileSizeBytes(long maxFileSizeBytes) {
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    public int getMaxFileNameBytes() {
        return maxFileNameBytes;
    }

    public void setMaxFileNameBytes(int maxFileNameBytes) {
        this.maxFileNameBytes = maxFileNameBytes;
    }
}
