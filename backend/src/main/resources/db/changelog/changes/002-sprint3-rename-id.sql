--liquibase formatted sql

--changeset Hyperka:002-sprint3-rename-id
ALTER TABLE file_metadata RENAME COLUMN id TO file_id;