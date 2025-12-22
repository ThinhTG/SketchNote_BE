-- V4: Add current_version_id column to user_resources table
-- This column tracks the version the user is currently using (can be upgraded)
-- Separate from purchased_version_id which tracks the original purchase

-- Add the new column
ALTER TABLE user_resources 
ADD COLUMN IF NOT EXISTS current_version_id BIGINT;

-- Migrate existing data: set current_version_id to purchased_version_id
-- This ensures existing users start with their purchased version
UPDATE user_resources 
SET current_version_id = purchased_version_id 
WHERE current_version_id IS NULL AND purchased_version_id IS NOT NULL;

-- Add comment for documentation
COMMENT ON COLUMN user_resources.current_version_id IS 'Version ID that user is currently using. Can be upgraded to newer versions for free.';
