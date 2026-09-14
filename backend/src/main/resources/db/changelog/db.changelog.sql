--liquibase formatted sql

--changeset author:file-manager id:001-create-file-metadata
CREATE TABLE file_metadata (
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

CREATE UNIQUE INDEX uq_file_metadata_storage_key
    ON file_metadata (storage_key);

CREATE INDEX idx_file_metadata_owner_created
    ON file_metadata (owner_id, created_at DESC);

CREATE INDEX idx_file_metadata_status
    ON file_metadata (status);

--changeset author:file-manager id:002-add-file-metadata-comments
COMMENT ON COLUMN file_metadata.id IS 'Public file identifier';
COMMENT ON COLUMN file_metadata.owner_id IS 'User or tenant that owns the file';
COMMENT ON COLUMN file_metadata.original_name IS 'Original client-side file name';
COMMENT ON COLUMN file_metadata.storage_key IS 'Provider object key; never derived from user input';
COMMENT ON COLUMN file_metadata.media_type IS 'Validated media type';
COMMENT ON COLUMN file_metadata.size_bytes IS 'Exact file size in bytes';
COMMENT ON COLUMN file_metadata.status IS 'File lifecycle status';
