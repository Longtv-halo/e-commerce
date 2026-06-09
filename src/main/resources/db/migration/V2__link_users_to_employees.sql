-- V2: Ensure employee_id link exists in users table (idempotent)
-- Column is already created in V1, this migration ensures FK constraint exists

ALTER TABLE users ADD COLUMN IF NOT EXISTS employee_id BIGINT;

DO $$
BEGIN
    ALTER TABLE users ADD CONSTRAINT fk_users_employee
        FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE SET NULL;
EXCEPTION WHEN duplicate_object THEN
    NULL;
END $$;
