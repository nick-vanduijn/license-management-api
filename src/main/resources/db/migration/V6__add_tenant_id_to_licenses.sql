-- Add tenant_id column to licenses table for schema-based multi-tenancy support
ALTER TABLE licenses ADD COLUMN tenant_id VARCHAR(255) NOT NULL DEFAULT 'default';
