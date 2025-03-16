-- Remove the Spring Batch comment as it's not relevant for this schema
-- Create and set schema
CREATE SCHEMA IF NOT EXISTS document_schema;

-- Switch to the document_schema
SET search_path TO document_schema;

-- Create documents table
CREATE TABLE IF NOT EXISTS document_schema.documents (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    document_name VARCHAR(255) NOT NULL,
    minio_path VARCHAR(512) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, document_name)
);

-- Create document_tags table for many-to-many relationship
CREATE TABLE IF NOT EXISTS document_schema.document_tags (
    document_id BIGINT NOT NULL,
    tag VARCHAR(100) NOT NULL,
    PRIMARY KEY (document_id, tag),
    FOREIGN KEY (document_id) REFERENCES document_schema.documents(id) ON DELETE CASCADE
);

-- Create indexes for efficient searching
CREATE INDEX IF NOT EXISTS idx_documents_user_id ON document_schema.documents(user_id);
CREATE INDEX IF NOT EXISTS idx_documents_document_name ON document_schema.documents(document_name);
CREATE INDEX IF NOT EXISTS idx_documents_created_at ON document_schema.documents(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_document_tags_tag ON document_schema.document_tags(tag);

-- Create composite index for user_id and document_name for faster lookups
CREATE INDEX IF NOT EXISTS idx_documents_user_document ON document_schema.documents(user_id, document_name);

