-- V2: Add employee_id link to users table
-- This allows ABAC to determine which department a user belongs to.

ALTER TABLE users ADD COLUMN IF NOT EXISTS employee_id BIGINT;
ALTER TABLE users ADD CONSTRAINT fk_users_employee
    FOREIGN KEY (employee_id) REFERENCES employees(id)
    ON DELETE SET NULL;
