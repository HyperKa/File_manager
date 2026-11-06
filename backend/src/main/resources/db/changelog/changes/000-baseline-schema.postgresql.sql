-- liquibase formatted sql

-- changeset PRIME:1793649895239-1 splitStatements:false
CREATE TABLE "file_metadata" ("id" UUID NOT NULL, "created_at" TIMESTAMP WITH TIME ZONE NOT NULL, "deleted_at" TIMESTAMP WITH TIME ZONE, "etag" VARCHAR(255), "media_type" VARCHAR(255) NOT NULL, "original_name" VARCHAR(255) NOT NULL, "owner_id" UUID NOT NULL, "sha256" VARCHAR(255), "size_bytes" BIGINT NOT NULL, "status" VARCHAR(255) NOT NULL, "storage_key" VARCHAR(255) NOT NULL, "updated_at" TIMESTAMP WITH TIME ZONE NOT NULL, "version" BIGINT NOT NULL, CONSTRAINT "file_metadata_pkey" PRIMARY KEY ("id"));

CREATE UNIQUE INDEX uq_file_metadata_storage_key
    ON file_metadata (storage_key);

-- для FM-2. Демонстрация первых 50 недавно созданных файлов
CREATE INDEX idx_file_metadata_owner_created
    ON file_metadata (owner_id, created_at DESC);

-- для демон-процесса, удаляющего помеченные FAILED файлы
CREATE INDEX idx_file_metadata_status
    ON file_metadata (status);