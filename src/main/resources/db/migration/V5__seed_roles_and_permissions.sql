-- V5: Seed default roles and permissions
-- This ensures ROLE_USER and ROLE_ADMIN exist so registration works correctly.

-- Seed basic roles
INSERT INTO roles (name)
VALUES ('ROLE_USER'),
       ('ROLE_ADMIN'),
       ('ROLE_MANAGER')
ON CONFLICT (name) DO NOTHING;

-- Seed basic permissions
INSERT INTO permissions (name)
VALUES
    -- Employee permissions
    ('EMPLOYEE_READ'),
    ('EMPLOYEE_WRITE'),
    ('EMPLOYEE_DELETE'),
    -- Department permissions
    ('DEPARTMENT_READ'),
    ('DEPARTMENT_WRITE'),
    ('DEPARTMENT_DELETE'),
    -- User permissions
    ('USER_READ'),
    ('USER_WRITE'),
    ('USER_DELETE'),
    -- Role permissions
    ('ROLE_READ'),
    ('ROLE_WRITE')
ON CONFLICT (name) DO NOTHING;

-- Assign permissions to ROLE_USER (read-only on employees and departments)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_USER'
  AND p.name IN ('EMPLOYEE_READ', 'DEPARTMENT_READ')
ON CONFLICT DO NOTHING;

-- Assign all permissions to ROLE_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;

-- Assign employee and department permissions to ROLE_MANAGER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_MANAGER'
  AND p.name IN (
      'EMPLOYEE_READ', 'EMPLOYEE_WRITE', 'EMPLOYEE_DELETE',
      'DEPARTMENT_READ', 'DEPARTMENT_WRITE'
  )
ON CONFLICT DO NOTHING;
