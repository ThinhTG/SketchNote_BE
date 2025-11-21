-- Seed data for subscription plans
-- Run this script after the application creates the tables

-- Insert Customer Pro plan
INSERT INTO subscription_plan (plan_name, plan_type, price, currency, duration_days, description, is_active, created_at, updated_at)
VALUES 
('Customer Pro - Monthly', 'CUSTOMER_PRO', 99000, 'VND', 30, 'Unlimited project creation for 30 days', true, NOW(), NOW()),
('Customer Pro - Yearly', 'CUSTOMER_PRO', 990000, 'VND', 365, 'Unlimited project creation for 1 year (2 months free)', true, NOW(), NOW());

-- Insert Designer plan
INSERT INTO subscription_plan (plan_name, plan_type, price, currency, duration_days, description, is_active, created_at, updated_at)
VALUES 
('Designer - Monthly', 'DESIGNER', 199000, 'VND', 30, 'Designer role with unlimited projects and selling capabilities for 30 days', true, NOW(), NOW()),
('Designer - Yearly', 'DESIGNER', 1990000, 'VND', 365, 'Designer role with unlimited projects and selling capabilities for 1 year (2 months free)', true, NOW(), NOW());

-- Verify the inserted data
SELECT * FROM subscription_plan ORDER BY plan_type, duration_days;
