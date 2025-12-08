-- =====================================================
-- State Machine Migration: isArchived → ARCHIVED status
-- =====================================================
-- This migration converts the boolean isArchived field to use the TemplateStatus enum
-- Following the State Machine diagram: PENDING_REVIEW → PUBLISHED → ARCHIVED/DELETED

-- Step 1: Add new status values to the enum (if using PostgreSQL)
-- Note: In MySQL, alter the enum definition directly
-- For PostgreSQL:
-- ALTER TYPE template_status ADD VALUE IF NOT EXISTS 'ARCHIVED';
-- ALTER TYPE template_status ADD VALUE IF NOT EXISTS 'DELETED';

-- Step 2: Migrate existing data
-- Convert isArchived = true to status = 'ARCHIVED'
UPDATE resource_template 
SET status = 'ARCHIVED' 
WHERE is_archived = true AND status = 'PUBLISHED';

-- Step 3: Drop the isArchived column (optional - can keep for rollback)
-- ALTER TABLE resource_template DROP COLUMN is_archived;

-- =====================================================
-- Verification query (run after migration)
-- =====================================================
-- SELECT status, COUNT(*) FROM resource_template GROUP BY status;
-- Expected: PENDING_REVIEW, PUBLISHED, REJECTED, ARCHIVED counts

-- =====================================================
-- Rollback script (if needed)
-- =====================================================
-- ALTER TABLE resource_template ADD COLUMN is_archived BOOLEAN DEFAULT false;
-- UPDATE resource_template SET is_archived = true WHERE status = 'ARCHIVED';
-- UPDATE resource_template SET status = 'PUBLISHED' WHERE status = 'ARCHIVED';
