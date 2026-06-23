# Database Reset & Migration Instructions

## Problem Analysis
The error occurs because:
1. Hibernate's `ddl-auto: update` created tables with incomplete/incorrect schemas
2. Conflicting data from previous registration attempts
3. Schema mismatches between entity definitions and database tables

## Solution: Reset Database & Rerun Migrations

### Option 1: Using PostgreSQL CLI (Recommended for Development)

#### Method A: Complete Database Reset

```bash
# 1. Connect to PostgreSQL as superuser
psql -h 192.168.102.10 -U longtv1 -d postgres

# 2. In psql terminal, execute:
DROP DATABASE IF EXISTS ecommerce;
CREATE DATABASE ecommerce;
\q

# 3. Done! Flyway will recreate everything when app starts
```

#### Method B: One-liner command

```bash
dropdb -h 192.168.102.10 -U longtv1 ecommerce 2>/dev/null; createdb -h 192.168.102.10 -U longtv1 ecommerce
```

#### Method C: Also clear Flyway history (if migrations failed)

```bash
psql -h 192.168.102.10 -U longtv1 -d ecommerce << EOF
DROP TABLE IF EXISTS flyway_schema_history CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS employees CASCADE;
DROP TABLE IF EXISTS departments CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS permissions CASCADE;
DROP TABLE IF EXISTS role_permissions CASCADE;
DROP TABLE IF EXISTS access_policies CASCADE;
EOF
```

### Option 2: Using pgAdmin GUI

1. Right-click on `ecommerce` database
2. Select "Delete/Drop"
3. Confirm deletion
4. Create new database named `ecommerce`

### Option 3: Docker (If using Docker Postgres)

```bash
# Reset the database container
docker-compose down
docker-compose up -d

# Or just reset the database
docker exec <postgres_container> psql -U longtv1 -d ecommerce -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
```

---

## Migration Execution Order (V1 → V12)

After resetting the database, the migrations will execute in this order:

| Version | Purpose |
|---------|---------|
| V1 | Baseline (placeholder) |
| V2 | Link users to employees |
| V3 | Add access policies table |
| V4 | Multi-tenant employees schema |
| V5 | Seed roles and permissions |
| V6 | Seed sample users |
| V7 | Refresh users and roles |
| V8 | Fix employees tenant_id nulls |
| V9 | Ensure users have roles |
| V10 | Ensure employees tenant_id schema |
| V11 | Fix users table schema - Remove extra columns |
| V12 | **[NEW]** Final users table recreate - Complete rebuild if needed |

---

## Steps to Complete

### 1. Reset Database
```bash
# Option A: PostgreSQL CLI
psql -h 192.168.102.10 -U longtv1 -d postgres
DROP DATABASE IF EXISTS ecommerce;
CREATE DATABASE ecommerce;
\q

# Or Option B: Single line
dropdb -h 192.168.102.10 -U longtv1 ecommerce && createdb -h 192.168.102.10 -U longtv1 ecommerce
```

### 2. Verify application.yaml configuration

Ensure `/Users/longtv/IdeaProjects/e-commerce/src/main/resources/application.yaml` has:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: none              # ✅ Let Flyway handle schema
  flyway:
    enabled: true                 # ✅ Enable Flyway
    baseline-on-migrate: true     # ✅ Auto-baseline
    baseline-version: 0           # ✅ Set baseline version
    locations: classpath:db/migration
```

### 3. Clean the project

```bash
cd /Users/longtv/IdeaProjects/e-commerce
mvn clean install
# Or in IDE: Build → Clean Project, then Build → Rebuild Project
```

### 4. Restart the Application

Your IDE will:
- ✅ Compile the project
- ✅ Copy migrations to target/classes
- ✅ Start Spring Boot application
- ✅ Flyway executes V1 through V11 migrations
- ✅ Create all tables with correct schema
- ✅ Seed 4 test users

---

## Test Accounts Ready After Migration

| Username | Password | Role |
|----------|----------|------|
| admin_new | demo123 | ROLE_ADMIN |
| manager_new | demo123 | ROLE_MANAGER |
| employee_new | demo123 | ROLE_USER |
| finance_new | demo123 | ROLE_USER |

---

## Verify Everything Works

```bash
# 1. Check if app started successfully
# Look for "Started Demo1Application" in console

# 2. Test authentication
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin_new","password":"demo123"}'

# Expected response:
# {
#   "success": true,
#   "results": {
#     "token": "eyJhbGciOiJIUzI1NiIs...",
#     "type": "Bearer",
#     "username": "admin_new",
#     "roles": ["ROLE_ADMIN"]
#   }
# }
```

---

## If Issues Persist

1. **Check Flyway status:**
   - Query database: `SELECT * FROM flyway_schema_history;`
   - Ensure all V1-V11 migrations are marked as SUCCESS

2. **Check schema:**
   - Users table: `\d users` (in psql)
   - Should have: id, name, username, password, employee_id, enabled
   - Should NOT have: tenant_id, role, is_tenant_admin

3. **Clear IDE cache:**
   - IDE → File → Invalidate Caches → Invalidate and Restart

---

## New Files Added

- ✅ V11__fix_users_table_schema.sql - Removes extra columns with CASCADE drops
- ✅ V12__final_users_table_recreate.sql - Complete table recreation if needed
- ✅ DATABASE_RESET_INSTRUCTIONS.md - This guide

After completing these steps, your e-commerce API will be fully functional with:
- ✅ Correct multi-tenant schema for employees
- ✅ Role-based access control (RBAC)
- ✅ Permission-based authorization
- ✅ User authentication with JWT tokens
- ✅ 4 test accounts ready to use

