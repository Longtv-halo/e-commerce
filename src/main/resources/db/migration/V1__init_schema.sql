-- V1: Complete initial schema
-- Creates all base tables: departments, employees, users, roles, permissions, user_roles, role_permissions

-- Departments table
CREATE TABLE IF NOT EXISTS departments (
    id        BIGSERIAL PRIMARY KEY,
    name      VARCHAR(255) NOT NULL,
    deleted   BOOLEAN DEFAULT FALSE NOT NULL,
    leader_id BIGINT
);

-- Employees table (with multi-tenancy support)
CREATE TABLE IF NOT EXISTS employees (
    id            BIGSERIAL PRIMARY KEY,
    tenant_id     VARCHAR(100) NOT NULL DEFAULT 'DEFAULT_TENANT',
    name          VARCHAR(255) NOT NULL,
    email         VARCHAR(255),
    department_id BIGINT REFERENCES departments(id) ON DELETE SET NULL,
    deleted       BOOLEAN DEFAULT FALSE NOT NULL,
    is_owner      BOOLEAN DEFAULT FALSE NOT NULL
);

-- Add leader_id FK to departments after employees exists
ALTER TABLE departments
    ADD CONSTRAINT  fk_dept_leader
    FOREIGN KEY (leader_id) REFERENCES employees(id) ON DELETE SET NULL;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255),
    username    VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    employee_id BIGINT REFERENCES employees(id) ON DELETE SET NULL,
    enabled     BOOLEAN DEFAULT TRUE NOT NULL
);

-- Roles table
CREATE TABLE IF NOT EXISTS roles (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- Permissions table
CREATE TABLE IF NOT EXISTS permissions (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- User-Roles join table
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Role-Permissions join table
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id       BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_employees_tenant ON employees (tenant_id);
CREATE INDEX IF NOT EXISTS idx_employees_dept   ON employees (department_id);
