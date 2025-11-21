-- =====================================================
-- SEED DATA FOR DASHBOARD (2015-2025)
-- =====================================================
-- This script generates realistic order data for dashboard visualization
-- Includes: Templates, Orders, and Order Details
-- Time range: 2015-01-01 to 2025-11-21
-- =====================================================

-- Step 1: Create sample resource templates for a designer
-- Assuming designer_id = 1 (adjust as needed)

INSERT INTO resource_template (designer_id, name, description, type, price, release_date, status, created_at, updated_at)
VALUES
    (1, 'Modern UI Kit', 'Complete modern UI components', 'TEMPLATES', 299000, '2015-01-15', 'PUBLISHED', '2015-01-15 10:00:00', '2015-01-15 10:00:00'),
    (1, 'Icon Pack Pro', 'Professional icon collection', 'ICONS', 149000, '2015-06-01', 'PUBLISHED', '2015-06-01 10:00:00', '2015-06-01 10:00:00'),
    (1, 'Business Mockups', 'Business card and branding mockups', 'MOCKUPS', 199000, '2016-03-10', 'PUBLISHED', '2016-03-10 10:00:00', '2016-03-10 10:00:00'),
    (1, 'Minimal Font Family', 'Clean and minimal font set', 'FONT', 99000, '2017-02-20', 'PUBLISHED', '2017-02-20 10:00:00', '2017-02-20 10:00:00'),
    (1, 'Illustration Bundle', 'Hand-drawn illustrations', 'ILLUSTRATIONS', 349000, '2018-05-15', 'PUBLISHED', '2018-05-15 10:00:00', '2018-05-15 10:00:00'),
    (1, 'Stock Photos Pack', 'High-quality stock photos', 'PHOTOS', 249000, '2019-01-10', 'PUBLISHED', '2019-01-10 10:00:00', '2019-01-10 10:00:00'),
    (1, 'Animated Titles', 'Motion graphics title templates', 'TITLES', 179000, '2020-04-05', 'PUBLISHED', '2020-04-05 10:00:00', '2020-04-05 10:00:00'),
    (1, 'Premium UI Components', 'Advanced UI component library', 'TEMPLATES', 399000, '2021-07-20', 'PUBLISHED', '2021-07-20 10:00:00', '2021-07-20 10:00:00'),
    (1, 'Social Media Kit', 'Complete social media templates', 'TEMPLATES', 229000, '2022-03-15', 'PUBLISHED', '2022-03-15 10:00:00', '2022-03-15 10:00:00'),
    (1, 'Dashboard Templates', 'Modern dashboard UI templates', 'TEMPLATES', 449000, '2023-01-10', 'PUBLISHED', '2023-01-10 10:00:00', '2023-01-10 10:00:00')
ON CONFLICT DO NOTHING;

-- Step 2: Generate orders with realistic patterns
-- Pattern: Growing revenue over years with seasonal variations
-- Higher sales in Q4 (holiday season), lower in Q1

DO $$
DECLARE
    v_year INT;
    v_month INT;
    v_day INT;
    v_order_date TIMESTAMP;
    v_order_id BIGINT;
    v_template_id BIGINT;
    v_user_id INT;
    v_price DECIMAL(15,2);
    v_orders_per_day INT;
    v_base_orders INT;
    v_growth_factor DECIMAL(5,2);
    v_seasonal_factor DECIMAL(5,2);
    v_random_factor DECIMAL(5,2);
BEGIN
    -- Loop through years 2015 to 2025
    FOR v_year IN 2015..2025 LOOP
        -- Calculate growth factor (increasing over years)
        v_growth_factor := 1.0 + ((v_year - 2015) * 0.15);
        
        -- Loop through months
        FOR v_month IN 1..12 LOOP
            -- Skip future months in 2025
            IF v_year = 2025 AND v_month > 11 THEN
                EXIT;
            END IF;
            
            -- Seasonal factor (higher in Q4, lower in Q1)
            v_seasonal_factor := CASE
                WHEN v_month IN (1, 2) THEN 0.7  -- Q1: Low
                WHEN v_month IN (3, 4, 5) THEN 0.9  -- Q2: Medium
                WHEN v_month IN (6, 7, 8) THEN 1.0  -- Q3: Normal
                WHEN v_month IN (9, 10) THEN 1.2  -- Early Q4: High
                ELSE 1.5  -- Nov-Dec: Very High (holiday season)
            END;
            
            -- Base orders per month
            v_base_orders := (15 * v_growth_factor * v_seasonal_factor)::INT;
            
            -- Loop through days in month
            FOR v_day IN 1..EXTRACT(DAY FROM (DATE_TRUNC('month', (v_year || '-' || v_month || '-01')::DATE) + INTERVAL '1 month - 1 day'))::INT LOOP
                -- Skip future dates in November 2025
                IF v_year = 2025 AND v_month = 11 AND v_day > 21 THEN
                    EXIT;
                END IF;
                
                -- Random variation (0.5 to 1.5)
                v_random_factor := 0.5 + (RANDOM() * 1.0);
                
                -- Orders for this day
                v_orders_per_day := GREATEST(1, (v_base_orders / 30.0 * v_random_factor)::INT);
                
                -- Create orders for this day
                FOR i IN 1..v_orders_per_day LOOP
                    -- Random time during the day
                    v_order_date := (v_year || '-' || LPAD(v_month::TEXT, 2, '0') || '-' || LPAD(v_day::TEXT, 2, '0'))::DATE 
                                    + (RANDOM() * INTERVAL '23 hours') 
                                    + (RANDOM() * INTERVAL '59 minutes');
                    
                    -- Random user (1-100)
                    v_user_id := 1 + (RANDOM() * 99)::INT;
                    
                    -- Random template (1-10, corresponding to templates created above)
                    v_template_id := 1 + (RANDOM() * 9)::INT;
                    
                    -- Get template price
                    SELECT price INTO v_price FROM resource_template WHERE template_id = v_template_id;
                    
                    -- Create order
                    INSERT INTO orders (user_id, resource_template_id, total_amount, payment_status, order_status, invoice_number, issue_date, created_at, updated_at)
                    VALUES (
                        v_user_id,
                        v_template_id,
                        v_price,
                        'PAID',
                        CASE WHEN RANDOM() > 0.1 THEN 'DELIVERED' ELSE 'CONFIRMED' END,
                        'INV-' || v_year || LPAD(v_month::TEXT, 2, '0') || LPAD(v_day::TEXT, 2, '0') || '-' || LPAD(i::TEXT, 4, '0'),
                        v_order_date,
                        v_order_date,
                        v_order_date
                    )
                    RETURNING order_id INTO v_order_id;
                    
                    -- Create order detail
                    INSERT INTO order_details (order_id, resource_template_id, unit_price, discount, subtotal_amount, created_at, updated_at)
                    VALUES (
                        v_order_id,
                        v_template_id,
                        v_price,
                        0,
                        v_price,
                        v_order_date,
                        v_order_date
                    );
                END LOOP;
            END LOOP;
        END LOOP;
        
        RAISE NOTICE 'Completed year %', v_year;
    END LOOP;
END $$;

-- Verify data
SELECT 
    EXTRACT(YEAR FROM issue_date) as year,
    COUNT(*) as total_orders,
    SUM(total_amount) as total_revenue
FROM orders
WHERE payment_status = 'PAID' 
  AND order_status IN ('CONFIRMED', 'DELIVERED')
GROUP BY EXTRACT(YEAR FROM issue_date)
ORDER BY year;
