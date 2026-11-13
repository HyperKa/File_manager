--liquibase formatted sql

--changeset author:file-manager id:001-create-file-metadata
CREATE TABLE IF NOT EXISTS file_metadata (
                               id UUID PRIMARY KEY,
                               owner_id UUID NOT NULL,
                               original_name TEXT NOT NULL,
                               storage_key TEXT NOT NULL,
                               media_type TEXT NOT NULL,
                               size_bytes BIGINT NOT NULL CHECK (size_bytes >= 0),
                               sha256 CHAR(64),
                               etag TEXT,
                               status TEXT NOT NULL CHECK (
                                   status IN ('PENDING', 'READY', 'FAILED', 'DELETING', 'DELETED')
                                   ),
                               created_at TIMESTAMPTZ NOT NULL,
                               updated_at TIMESTAMPTZ NOT NULL,
                               deleted_at TIMESTAMPTZ,
                               version BIGINT NOT NULL DEFAULT 0
);

-- гарантия на уровне бд unique значения
CREATE UNIQUE INDEX IF NOT EXISTS uq_file_metadata_storage_key
    ON file_metadata (storage_key);

-- для FM-2. Демонстрация первых 50 недавно созданных файлов
CREATE INDEX IF NOT EXISTS idx_file_metadata_owner_created
    ON file_metadata (owner_id, created_at DESC);

-- для демон-процесса, удаляющего помеченные FAILED файлы
CREATE INDEX IF NOT EXISTS idx_file_metadata_status
    ON file_metadata (status);