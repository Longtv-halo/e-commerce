-- V12: Final users table schema fix - Recreate if necessary
-- This migration ensures users table has absolutely correct schema

-- Step 1: Check if role column exists and has NOT NULL constraint
-- If it does, we need to completely recreate the table

DO $$
DECLARE
    has_role_column BOOLEAN;
    col_is_not_null BOOLEAN;
BEGIN
    -- Check if role column exists
    SELECT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name='users' AND column_name='role'
    ) INTO has_role_column;

    IF has_role_column THEN
        -- Check if it's NOT NULL
        SELECT NOT is_nullable FROM information_schema.columns
        WHERE table_name='users' AND column_name='role' INTO col_is_not_null;

        IF col_is_not_null THEN
            -- Save existing user data
            CREATE TEMP TABLE temp_users AS
            SELECT id, name, username, password, employee_id, enabled
            FROM users;

            -- Drop the users table completely
            DROP TABLE IF EXISTS user_roles CASCADE;
            DROP TABLE IF EXISTS users CASCADE;

            -- Recreate users table with correct schema
            CREATE TABLE users (
                id BIGSERIAL PRIMARY KEY,
                name VARCHAR(255),
                username VARCHAR(255) NOT NULL UNIQUE,
                password VARCHAR(255) NOT NULL,
                employee_id BIGINT,
                enabled BOOLEAN DEFAULT TRUE NOT NULL
            );

            -- Recreate foreign key
            ALTER TABLE users ADD CONSTRAINT fk_users_employee
                FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE SET NULL;

            -- Recreate user_roles table
            CREATE TABLE IF NOT EXISTS user_roles (
                user_id BIGINT NOT NULL,
                role_id BIGINT NOT NULL,
                PRIMARY KEY (user_id, role_id),
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
            );

            -- Restore user data
            INSERT INTO users (id, name, username, password, employee_id, enabled)
            SELECT id, name, username, password, employee_id, enabled FROM temp_users
            ON CONFLICT (username) DO NOTHING;

            -- Drop temp table
            DROP TABLE temp_users;
        END IF;
    END IF;
END $$;

-- Step 2: Ensure final structure is correct
-- Add any missing columns
ALTER TABLE users ADD COLUMN IF NOT EXISTS id BIGSERIAL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS name VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS username VARCHAR(255) NOT NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS password VARCHAR(255) NOT NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS employee_id BIGINT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS enabled BOOLEAN DEFAULT TRUE NOT NULL;

-- Step 3: Ensure the lookup tables exist
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Step 4: Clean up any duplicate users by username
DELETE FROM user_roles WHERE user_id IN (
    SELECT id FROM users u1
    WHERE u1.id NOT IN (
        SELECT MAX(id) FROM users u2
        WHERE u2.username = u1.username
    )
);

DELETE FROM users u1
WHERE u1.id NOT IN (
    SELECT MAX(id) FROM users u2
    WHERE u2.username = u1.username
);

-- Step 5: Verify structure - should have only these columns
-- id, name, username, password, employee_id, enabled
-- NO: role, tenant_id, is_tenant_admin

