-- V7: Drop and refresh user and user_roles data
-- This migration clears old test data and creates fresh seed data

-- Delete old user_roles assignments
DELETE FROM user_roles
WHERE user_id IN (SELECT id FROM users WHERE username IN ('admin', 'manager', 'user'));

-- Delete old test users
DELETE FROM users
WHERE username IN ('admin', 'manager', 'user');

-- Insert fresh sample users
INSERT INTO users (name, username, password, enabled)
VALUES
    ('Quản Lý Hệ Thống', 'admin_new', '$2a$10$9YqyxVjrUVfGDsJkFXN9ouB5p6igXYNqVqBvKNgA8rCGVRIi0Wy7G', true),
    ('Quản Lý Phòng Ban', 'manager_new', '$2a$10$5h4G7NvP/GlhE3Ky9WmX5eJ2mL8sK9vR4tUxQzYdAb6CfDnO2K1B2', true),
    ('Nhân Viên Thường', 'employee_new', '$2a$10$3D/gP7mK2xL5hR8nV1wQ3uT4zZ9sA6bC8fJ2eN0cM7dL4pO1kX9Y6', true),
    ('Nhân Viên Tài Chính', 'finance_new', '$2a$10$7K8hJ3nM2pL5oQ1rS9tU4vW6xY7zA8bC9dE0fG1hI2jK3lM4nO5p', true)
ON CONFLICT (username) DO NOTHING;

-- Assign ROLE_ADMIN to admin_new
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin_new' AND r.name = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;

-- Assign ROLE_MANAGER to manager_new
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'manager_new' AND r.name = 'ROLE_MANAGER'
ON CONFLICT DO NOTHING;

-- Assign ROLE_USER to both employee accounts
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username IN ('employee_new', 'finance_new') AND r.name = 'ROLE_USER'
ON CONFLICT DO NOTHING;

