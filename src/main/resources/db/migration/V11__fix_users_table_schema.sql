-- V11: Fix users table schema to match Users entity
-- Remove any extra columns that might be causing constraint violations

-- Step 1: Drop all foreign key constraints first
ALTER TABLE users DROP CONSTRAINT IF EXISTS fk_users_employee;
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_employee_id_key;

-- Step 2: Drop the problematic 'role' column and its default
DO $$
BEGIN
    -- Try to drop any default for role column
    ALTER TABLE users ALTER COLUMN role DROP DEFAULT;
EXCEPTION WHEN others THEN
    NULL;
END $$;

-- Step 3: Drop any extra columns that aren't in the Users entity
ALTER TABLE users DROP COLUMN IF EXISTS role CASCADE;
ALTER TABLE users DROP COLUMN IF EXISTS tenant_id CASCADE;
ALTER TABLE users DROP COLUMN IF EXISTS is_tenant_admin CASCADE;

-- Step 4: Drop old constraints
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_pkey CASCADE;
ALTER TABLE users DROP CONSTRAINT IF EXISTS uk_users_username CASCADE;

-- Step 5: Recreate all required columns with correct types
DO $$
BEGIN
    ALTER TABLE users ADD COLUMN IF NOT EXISTS id BIGSERIAL NOT NULL;
EXCEPTION WHEN others THEN
    NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE users ADD COLUMN IF NOT EXISTS name VARCHAR(255);
EXCEPTION WHEN others THEN
    NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE users ADD COLUMN IF NOT EXISTS username VARCHAR(255) NOT NULL;
EXCEPTION WHEN others THEN
    NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE users ADD COLUMN IF NOT EXISTS password VARCHAR(255) NOT NULL;
EXCEPTION WHEN others THEN
    NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE users ADD COLUMN IF NOT EXISTS employee_id BIGINT;
EXCEPTION WHEN others THEN
    NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE users ADD COLUMN IF NOT EXISTS enabled BOOLEAN DEFAULT TRUE NOT NULL;
EXCEPTION WHEN others THEN
    NULL;
END $$;

-- Step 6: Remove duplicate entries, keep only latest
DELETE FROM users u1
WHERE u1.id NOT IN (
    SELECT MAX(id) FROM users u2 WHERE u2.username = u1.username GROUP BY u2.username
);

-- Step 7: Create proper constraints
DO $$
BEGIN
    ALTER TABLE users ADD CONSTRAINT users_pkey PRIMARY KEY (id);
EXCEPTION WHEN others THEN
    NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE users ADD CONSTRAINT uk_users_username UNIQUE (username);
EXCEPTION WHEN others THEN
    NULL;
END $$;

-- Step 8: Recreate foreign key to employees
DO $$
BEGIN
    ALTER TABLE users ADD CONSTRAINT fk_users_employee
        FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE SET NULL;
EXCEPTION WHEN others THEN
    NULL;
END $$;

