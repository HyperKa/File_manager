--liquibase formatted sql

--changeset Hyperka:001-sprint3-schema
--comment: Добавление CHECK-ограничений и индексов для новых фич Спринта 3

-- 1. Гарантии целостности данных (CHECK constraints)
ALTER TABLE file_metadata DROP CONSTRAINT IF EXISTS chk_size_bytes_positive;
ALTER TABLE file_metadata ADD CONSTRAINT chk_size_bytes_positive CHECK (size_bytes >= 0);

ALTER TABLE file_metadata DROP CONSTRAINT IF EXISTS chk_status_valid;
ALTER TABLE file_metadata ADD CONSTRAINT chk_status_valid CHECK (status IN ('PENDING', 'READY', 'FAILED', 'DELETING', 'DELETED'));

-- 2. Индекс для дедупликации файлов (задача BE-2)
CREATE INDEX IF NOT EXISTS idx_file_metadata_sha256
    ON file_metadata (sha256);

-- 3. Индекс для оптимизации Soft Delete и фоновых процессов (задача BE-4)
CREATE INDEX IF NOT EXISTS idx_file_metadata_status_deleted
    ON file_metadata (status, deleted_at);

--rollback ALTER TABLE file_metadata DROP CONSTRAINT IF EXISTS chk_size_bytes_positive;
--rollback ALTER TABLE file_metadata DROP CONSTRAINT IF EXISTS chk_status_valid;
--rollback DROP INDEX IF EXISTS idx_file_metadata_sha256;
--rollback DROP INDEX IF EXISTS idx_file_metadata_status_deleted;