-- V8: Fix tenant_id null values in employees table
-- This migration updates all existing employees with null tenant_id to a default value

-- Update null tenant_id values to default tenant
UPDATE employees
SET tenant_id = 'DEFAULT_TENANT'
WHERE tenant_id IS NULL;

-- Verify the update (optional, for logging)
-- SELECT COUNT(*) as employees_with_default_tenant FROM employees WHERE tenant_id = 'DEFAULT_TENANT';

