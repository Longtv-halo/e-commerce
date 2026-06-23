-- V4: Ensure employees table has multi-tenant support (idempotent)
-- Table is already created in V1, this migration ensures RLS and tenant isolation

-- Add tenant_id if somehow missing
ALTER TABLE employees ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(100) NOT NULL DEFAULT 'DEFAULT_TENANT';

-- Update any null tenant_id values
UPDATE employees SET tenant_id = 'DEFAULT_TENANT' WHERE tenant_id IS NULL;

-- Enable Row Level Security
ALTER TABLE employees ENABLE ROW LEVEL SECURITY;
ALTER TABLE employees FORCE ROW LEVEL SECURITY;

-- Create tenant isolation policy (skip if already exists)
DO $$
BEGIN
    CREATE POLICY tenant_isolation ON employees
        USING (tenant_id = current_setting('app.current_tenant', TRUE));
EXCEPTION WHEN duplicate_object THEN
    NULL;
END $$;

-- Index for performance
CREATE INDEX IF NOT EXISTS idx_employees_tenant ON employees (tenant_id);

