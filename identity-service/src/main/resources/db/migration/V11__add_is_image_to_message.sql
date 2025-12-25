-- Add is_image column to message table

ALTER TABLE message ADD COLUMN IF NOT EXISTS is_image BOOLEAN NOT NULL DEFAULT FALSE;

-- Add comment
COMMENT ON COLUMN message.is_image IS 'Flag to indicate if message content is an image URL (true) or text (false)';
