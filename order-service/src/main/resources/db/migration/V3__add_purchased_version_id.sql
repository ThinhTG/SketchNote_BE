-- Migration: Add purchased_version_id to user_resources table
-- This field tracks which version the user purchased, allowing them to access
-- their purchased version + all newer versions (free upgrade policy)

ALTER TABLE user_resources 
ADD COLUMN IF NOT EXISTS purchased_version_id BIGINT NULL;

-- Add foreign key constraint (optional, depends on your database design preference)
-- ALTER TABLE user_resources 
-- ADD CONSTRAINT fk_user_resources_purchased_version 
-- FOREIGN KEY (purchased_version_id) REFERENCES resource_template_version(version_id);

-- Create index for better query performance
CREATE INDEX IF NOT EXISTS idx_user_resources_purchased_version 
ON user_resources(purchased_version_id);

-- Comment explaining the field
COMMENT ON COLUMN user_resources.purchased_version_id IS 
'The version ID that user purchased. User can access this version + all newer versions (free upgrade policy).';
