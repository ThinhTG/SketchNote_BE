-- Add project_id column to notification table for project collaboration notifications
ALTER TABLE notification
ADD COLUMN project_id BIGINT NULL COMMENT 'Optional reference to a project';

-- Add index for better query performance when filtering by project_id
CREATE INDEX idx_notification_project ON notification(project_id);
