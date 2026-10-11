package org.example.file_manager_cloudflare.file.domain;

import java.io.InputStream;

// Интерфейс, определяющий контракт для загрузки, получения типа, размера файла
public interface DownloadHandle extends AutoCloseable {

    InputStream inputStream();

    String contentType();

    long sizeBytes();

    String eTag();

    @Override
    void close() throws Exception;
}
