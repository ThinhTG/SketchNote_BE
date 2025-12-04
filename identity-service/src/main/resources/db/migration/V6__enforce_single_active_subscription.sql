-- =====================================================
-- SUBSCRIPTION DESIGN: 1-N with Business Rule Enforcement
-- =====================================================
-- 
-- Design Decision: Keep 1-N relationship (User has many Subscriptions)
-- But enforce: Only 1 ACTIVE subscription per user at any time
--
-- Benefits:
-- 1. Full subscription history for audit/reporting
-- 2. Revenue tracking per transaction
-- 3. Customer support can view history
-- 4. Analytics: upgrade/downgrade patterns
--
-- =====================================================

-- Option 1: Partial Unique Index (PostgreSQL)
-- This ensures only ONE active subscription per user
CREATE UNIQUE INDEX IF NOT EXISTS idx_user_subscription_unique_active 
ON user_subscription (user_id) 
WHERE status = 'ACTIVE';

-- Option 2: If above doesn't work, use a trigger
-- This trigger prevents inserting/updating to ACTIVE if one already exists

-- View to check current state
-- SELECT user_id, COUNT(*) as active_count 
-- FROM user_subscription 
-- WHERE status = 'ACTIVE' 
-- GROUP BY user_id 
-- HAVING COUNT(*) > 1;

-- Fix existing duplicates (keep latest, cancel others)
WITH ranked AS (
    SELECT 
        subscription_id,
        user_id,
        ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY start_date DESC) as rn
    FROM user_subscription
    WHERE status = 'ACTIVE'
)
UPDATE user_subscription 
SET status = 'CANCELLED'
WHERE subscription_id IN (
    SELECT subscription_id FROM ranked WHERE rn > 1
);

-- Verify fix
SELECT user_id, COUNT(*) as active_count 
FROM user_subscription 
WHERE status = 'ACTIVE' 
GROUP BY user_id 
HAVING COUNT(*) > 1;
