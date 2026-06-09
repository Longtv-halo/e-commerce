-- V10: Ensure employees table has all required columns for multi-tenancy
-- This migration adds missing tenant_id column and other required fields

-- Create employees table if it doesn't exist (for fresh databases)
CREATE TABLE IF NOT EXISTS employees (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    department_id BIGINT,
    deleted BOOLEAN DEFAULT FALSE,
    is_owner BOOLEAN DEFAULT FALSE NOT NULL
);

-- For existing databases, add missing columns

-- Step 1: Add tenant_id column if it doesn't exist
DO $$
BEGIN
    ALTER TABLE employees
    ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(100);
EXCEPTION WHEN others THEN
    NULL;
END $$;

-- Step 2: Set default values for any null tenant_id
UPDATE employees
SET tenant_id = 'DEFAULT_TENANT'
WHERE tenant_id IS NULL;

-- Step 3: Add NOT NULL constraint to tenant_id
DO $$
BEGIN
    ALTER TABLE employees
    ALTER COLUMN tenant_id SET NOT NULL;
EXCEPTION WHEN others THEN
    NULL;
END $$;

-- Step 4: Add is_owner column if it doesn't exist
DO $$
BEGIN
    ALTER TABLE employees
    ADD COLUMN IF NOT EXISTS is_owner BOOLEAN DEFAULT FALSE NOT NULL;
EXCEPTION WHEN others THEN
    NULL;
END $$;

-- Step 5: Add indexes for multi-tenant queries
CREATE INDEX IF NOT EXISTS idx_employees_tenant ON employees (tenant_id);
CREATE INDEX IF NOT EXISTS idx_employees_tenant_id ON employees (tenant_id, id);

-- Step 6: Add foreign key constraint for departments if it doesn't exist
DO $$
BEGIN
    ALTER TABLE employees
    ADD CONSTRAINT fk_employees_department
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL;
EXCEPTION WHEN others THEN
    NULL;
END $$;

-- Step 7: Enable Row Level Security (RLS) for multi-tenancy
DO $$
BEGIN
    ALTER TABLE employees ENABLE ROW LEVEL SECURITY;
    ALTER TABLE employees FORCE ROW LEVEL SECURITY;
EXCEPTION WHEN others THEN
    NULL;
END $$;

-- Step 8: Create tenant isolation policy if it doesn't exist
DO $$
BEGIN
    CREATE POLICY tenant_isolation ON employees
        USING (tenant_id = current_setting('app.current_tenant', TRUE));
EXCEPTION WHEN others THEN
    NULL;
END $$;
