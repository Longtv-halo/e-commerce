-- V6: Seed sample users and assign roles
-- This migration creates sample users with different roles for testing

-- Delete old test users if they exist
DELETE FROM user_roles WHERE user_id IN (
    SELECT id FROM users WHERE username IN ('admin', 'manager', 'user', 'johndoe_new')
);
DELETE FROM users WHERE username IN ('admin', 'manager', 'user', 'johndoe_new');

-- Seed sample users
INSERT INTO users (name, username, password, enabled)
VALUES
    ('Admin User', 'admin', '$2a$10$9YqyxVjrUVfGDsJkFXN9ouB5p6igXYNqVqBvKNgA8rCGVRIi0Wy7G', true),
    ('Manager User', 'manager', '$2a$10$5h4G7NvP/GlhE3Ky9WmX5eJ2mL8sK9vR4tUxQzYdAb6CfDnO2K1B2', true),
    ('Regular User', 'user', '$2a$10$3D/gP7mK2xL5hR8nV1wQ3uT4zZ9sA6bC8fJ2eN0cM7dL4pO1kX9Y6', true)
ON CONFLICT (username) DO NOTHING;

-- Assign ROLE_ADMIN to admin user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;

-- Assign ROLE_MANAGER to manager user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'manager' AND r.name = 'ROLE_MANAGER'
ON CONFLICT DO NOTHING;

-- Assign ROLE_USER to regular user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'user' AND r.name = 'ROLE_USER'
ON CONFLICT DO NOTHING;

