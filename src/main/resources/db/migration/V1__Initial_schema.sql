-- Create extension for UUID generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create plans enum type
CREATE TYPE plan_type AS ENUM ('BASIC', 'PREMIUM', 'ENTERPRISE');

-- Create license_status enum type  
CREATE TYPE license_status_type AS ENUM ('ACTIVE', 'SUSPENDED', 'REVOKED', 'EXPIRED');

-- Organizations table
CREATE TABLE organizations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    contact_email VARCHAR(255) NOT NULL,
    plan plan_type NOT NULL DEFAULT 'BASIC',
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    CONSTRAINT org_tenant_email_unique UNIQUE (tenant_id, contact_email)
);

-- Licenses table
CREATE TABLE licenses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id VARCHAR(255) NOT NULL,
    organization_id UUID NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    status license_status_type NOT NULL DEFAULT 'ACTIVE',
    issued_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP WITH TIME ZONE,
    features JSONB,
    signature TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    CONSTRAINT fk_license_organization FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE
);

-- Audit logs table
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id VARCHAR(255) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    user_id VARCHAR(255),
    details JSONB,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Create indexes for performance
CREATE INDEX idx_organizations_tenant_id ON organizations(tenant_id);
CREATE INDEX idx_organizations_contact_email ON organizations(contact_email);
CREATE INDEX idx_organizations_plan ON organizations(plan);
CREATE INDEX idx_organizations_active ON organizations(active);

CREATE INDEX idx_licenses_tenant_id ON licenses(tenant_id);
CREATE INDEX idx_licenses_organization_id ON licenses(organization_id);
CREATE INDEX idx_licenses_customer_email ON licenses(customer_email);
CREATE INDEX idx_licenses_status ON licenses(status);
CREATE INDEX idx_licenses_expires_at ON licenses(expires_at);

CREATE INDEX idx_audit_logs_tenant_id ON audit_logs(tenant_id);
CREATE INDEX idx_audit_logs_entity_type ON audit_logs(entity_type);
CREATE INDEX idx_audit_logs_entity_id ON audit_logs(entity_id);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);

-- Create partial indexes for frequently queried data
CREATE INDEX idx_licenses_active ON licenses(tenant_id, status) WHERE status = 'ACTIVE';
CREATE INDEX idx_licenses_expiring ON licenses(expires_at) WHERE expires_at IS NOT NULL AND status = 'ACTIVE';

-- Add comments for documentation
COMMENT ON TABLE organizations IS 'Multi-tenant organizations table';
COMMENT ON TABLE licenses IS 'Software licenses with digital signatures';
COMMENT ON TABLE audit_logs IS 'Audit trail for all system operations';

COMMENT ON COLUMN organizations.tenant_id IS 'Tenant identifier for multi-tenancy';
COMMENT ON COLUMN licenses.features IS 'JSON object containing licensed features';
COMMENT ON COLUMN licenses.signature IS 'Digital signature for license integrity';
COMMENT ON COLUMN audit_logs.details IS 'JSON object containing operation details';
