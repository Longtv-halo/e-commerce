-- V3: Create access_policies table for ABAC
-- Generic policy table that can control access to any resource_type,
-- for any subject_type, with time-based attributes.

CREATE TABLE IF NOT EXISTS access_policies (
    id            BIGSERIAL PRIMARY KEY,

    -- Resource being protected (e.g., DEPARTMENT, SERVICE, REPORT)
    resource_type VARCHAR(50)  NOT NULL,
    -- NULL means "applies to ALL resources of this type"
    resource_id   BIGINT,

    -- Who this policy applies to
    subject_type  VARCHAR(50)  NOT NULL DEFAULT 'DEPARTMENT',   -- DEPARTMENT | ROLE | USER
    subject_id    BIGINT       NOT NULL,

    -- Time constraints (environment attributes)
    -- ALL | MON | TUE | WED | THU | FRI | SAT | SUN
    day_of_week   VARCHAR(10)  NOT NULL DEFAULT 'ALL',
    start_time    TIME         NOT NULL,
    end_time      TIME         NOT NULL,

    -- ALLOW or DENY
    effect        VARCHAR(10)  NOT NULL DEFAULT 'ALLOW',

    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    description   VARCHAR(255)
);

-- Index for fast lookup when evaluating policies
CREATE INDEX IF NOT EXISTS idx_ap_resource ON access_policies (resource_type, resource_id);
CREATE INDEX IF NOT EXISTS idx_ap_subject  ON access_policies (subject_type, subject_id);
CREATE INDEX IF NOT EXISTS idx_ap_enabled  ON access_policies (enabled);
