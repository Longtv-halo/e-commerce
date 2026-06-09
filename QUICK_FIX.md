# QUICK FIX - Copy & Paste These Commands

## Problem
Database has a "role" column with NOT NULL constraint that causes registration to fail.

## Solution
Run ONE of these commands to reset your database:

### Option 1: Simple One-Liner (RECOMMENDED)
```bash
dropdb -h 192.168.102.10 -U longtv1 ecommerce 2>/dev/null; createdb -h 192.168.102.10 -U longtv1 ecommerce
```

### Option 2: Full Reset (using psql)
```bash
psql -h 192.168.102.10 -U longtv1 -d postgres -c "DROP DATABASE IF EXISTS ecommerce; CREATE DATABASE ecommerce;"
```

### Option 3: Interactive (if above doesn't work)
```bash
# Step 1
psql -h 192.168.102.10 -U longtv1 -d postgres

# Step 2 (inside psql terminal)
DROP DATABASE IF EXISTS ecommerce;
CREATE DATABASE ecommerce;
\q
```

---

## After Running Database Reset

1. **Clean and rebuild project:**
```bash
cd /Users/longtv/IdeaProjects/e-commerce
mvn clean install
```

2. **Restart your application** (in IDE or terminal)

3. **Wait for Flyway to execute 12 migrations** (V1 → V12)

4. **Test it works:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin_new","password":"demo123"}'
```

---

## Test Accounts

After migrations complete:

| Username | Password |
|----------|----------|
| admin_new | demo123 |
| manager_new | demo123 |
| employee_new | demo123 |
| finance_new | demo123 |

---

## What Migrations Do

| V# | Action |
|----|--------|
| V1-V4 | Create base tables (users, employees, roles, etc.) |
| V5 | Seed roles and permissions |
| V6 | Seed test users |
| V7 | Refresh user data |
| V8-V10 | Fix schema issues (tenant_id, roles) |
| V11 | Remove bad columns (role, tenant_id from users) |
| V12 | Recreate users table if still has issues |

---

## If Still Getting Errors

1. Check migration history:
```bash
psql -h 192.168.102.10 -U longtv1 -d ecommerce -c "SELECT * FROM flyway_schema_history;"
```

2. Clear EVERYTHING and start fresh:
```bash
psql -h 192.168.102.10 -U longtv1 << 'EOF'
DROP DATABASE IF EXISTS ecommerce;
CREATE DATABASE ecommerce;
\q
EOF
```

3. Clear IDE cache and rebuild:
   - Close project in IDE
   - Delete `/Users/longtv/IdeaProjects/e-commerce/target/` directory
   - Reopen project in IDE
   - Run `mvn clean install`

---

## Expected Success Indicators

✅ Application starts without errors  
✅ Can call `/api/auth/login` successfully  
✅ Get JWT token in response  
✅ Can access protected endpoints with token  

