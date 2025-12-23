-- Add password column to users table for storing encrypted passwords
-- This enables Google OAuth login for existing users by storing their password in a reversible format

ALTER TABLE users ADD COLUMN IF NOT EXISTS password VARCHAR(500);

COMMENT ON COLUMN users.password IS 'Encrypted password (AES) - used for Google OAuth login flow';
