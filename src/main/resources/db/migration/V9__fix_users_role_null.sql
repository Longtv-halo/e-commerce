-- V9: Ensure all users have at least one role
-- This migration assigns ROLE_USER to any users that don't have a role yet

-- Assign ROLE_USER to any users that don't have any role in user_roles table
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE r.name = 'ROLE_USER'
  AND NOT EXISTS (
      SELECT 1 FROM user_roles ur
      WHERE ur.user_id = u.id
  )
ON CONFLICT DO NOTHING;

