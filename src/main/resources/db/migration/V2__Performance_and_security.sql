-- Add additional indexes for production performance optimization

-- Composite indexes for common query patterns
CREATE INDEX idx_licenses_tenant_org_status ON licenses(tenant_id, organization_id, status);
CREATE INDEX idx_licenses_tenant_customer ON licenses(tenant_id, customer_email);
CREATE INDEX idx_audit_logs_tenant_entity ON audit_logs(tenant_id, entity_type, entity_id);

-- GIN index for JSONB features column for feature-based queries
CREATE INDEX idx_licenses_features_gin ON licenses USING GIN (features);

-- Partial index for active organizations
CREATE INDEX idx_organizations_active_tenant ON organizations(tenant_id, plan) WHERE active = true;

-- Add row-level security policies for tenant isolation
ALTER TABLE organizations ENABLE ROW LEVEL SECURITY;
ALTER TABLE licenses ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_logs ENABLE ROW LEVEL SECURITY;

-- Function to get current tenant from application context
CREATE OR REPLACE FUNCTION get_current_tenant()
RETURNS TEXT AS $$
BEGIN
    -- This will be set by the application at connection time
    RETURN current_setting('app.current_tenant', true);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- RLS policies for tenant isolation
CREATE POLICY organizations_tenant_isolation ON organizations
    FOR ALL
    USING (tenant_id = get_current_tenant());

CREATE POLICY licenses_tenant_isolation ON licenses
    FOR ALL
    USING (tenant_id = get_current_tenant());

CREATE POLICY audit_logs_tenant_isolation ON audit_logs
    FOR ALL
    USING (tenant_id = get_current_tenant());

-- Add trigger for automatic updated_at timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_organizations_updated_at
    BEFORE UPDATE ON organizations
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_licenses_updated_at
    BEFORE UPDATE ON licenses
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
