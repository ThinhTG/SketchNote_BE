-- Ensure ARCHIVED/DELETED are accepted values for resource_template.status
-- This fixes Postgres constraint resource_template_status_check blocking archive action.

-- Add enum values when using an ENUM type (PostgreSQL safe add)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_type WHERE typname = 'template_status') THEN
        BEGIN
            ALTER TYPE template_status ADD VALUE IF NOT EXISTS 'ARCHIVED';
        EXCEPTION WHEN duplicate_object THEN NULL; END;
        BEGIN
            ALTER TYPE template_status ADD VALUE IF NOT EXISTS 'DELETED';
        EXCEPTION WHEN duplicate_object THEN NULL; END;
    END IF;
END$$;

-- Refresh the status check constraint to include ARCHIVED/DELETED (PostgreSQL)
ALTER TABLE resource_template DROP CONSTRAINT IF EXISTS resource_template_status_check;
ALTER TABLE resource_template
    ADD CONSTRAINT resource_template_status_check
    CHECK (status IN ('PENDING_REVIEW','PUBLISHED','REJECTED','ARCHIVED','DELETED'));

-- Backfill: migrate any legacy is_archived=true rows into the new ARCHIVED status
UPDATE resource_template
SET status = 'ARCHIVED'
WHERE COALESCE(is_archived, false) = true
  AND status <> 'ARCHIVED';
