-- Add COLLABORATION to notification_type_check constraint

-- Drop old constraint
ALTER TABLE notification DROP CONSTRAINT IF EXISTS notification_type_check;

-- Add new constraint with COLLABORATION included
ALTER TABLE notification ADD CONSTRAINT notification_type_check 
    CHECK (type IN ('PURCHASE', 'PURCHASE_CONFIRM', 'SYSTEM', 'COMMENT', 'ENROLLMENT', 'SUBSCRIPTION', 'WALLET', 'VERSION_AVAILABLE', 'COLLABORATION', 'REJECT_BLOG'));
