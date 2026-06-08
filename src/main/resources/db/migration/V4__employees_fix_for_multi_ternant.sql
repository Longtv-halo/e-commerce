-- Employees table (multi-tenant version)
CREATE TABLE employees (
    id            BIGSERIAL,
    tenant_id     VARCHAR(100) NOT NULL,
    name          VARCHAR(255) NOT NULL,
    email         VARCHAR(255),
    department_id BIGINT,
    deleted       BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (id, tenant_id)
);

-- Enable RLS
ALTER TABLE employees ENABLE ROW LEVEL SECURITY;
ALTER TABLE employees FORCE ROW LEVEL SECURITY;

-- Policy: mỗi tenant chỉ thấy data của mình
CREATE POLICY tenant_isolation ON employees
    USING (tenant_id = current_setting('app.current_tenant', TRUE));

-- Index quan trọng cho performance
CREATE INDEX idx_employees_tenant ON employees (tenant_id);