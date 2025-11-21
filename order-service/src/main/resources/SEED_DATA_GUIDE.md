# Dashboard Seed Data Guide

## Overview

Tạo seed data cho dashboard từ **2015 đến 2025** với pattern tăng trưởng thực tế và biến động theo mùa.

---

## Files Created

### 1. [seed_dashboard_data.sql](file:///d:/Ki9_DoAn/21th11Sketcnote/SketchNote_BE/order-service/src/main/resources/seed_dashboard_data.sql)

SQL script tự động tạo data với các đặc điểm:

**Features:**
- ✅ 10 resource templates đa dạng (UI Kit, Icons, Mockups, etc.)
- ✅ Orders từ 2015-01-01 đến 2025-11-21
- ✅ Tăng trưởng 15% mỗi năm
- ✅ Seasonal variations:
  - Q1 (Jan-Feb): 70% baseline (thấp)
  - Q2 (Mar-May): 90% baseline
  - Q3 (Jun-Aug): 100% baseline
  - Q4 (Sep-Oct): 120% baseline
  - Holiday (Nov-Dec): 150% baseline (cao nhất)
- ✅ Random variations để data trông tự nhiên
- ✅ Tất cả orders đều PAID và DELIVERED/CONFIRMED

**Estimated Data Volume:**
- ~10 templates
- ~15,000-20,000 orders (tùy random factor)
- Revenue tăng dần từ 2015 → 2025

---

## How to Run

### Option 1: Using psql Command Line

```bash
# Connect to database
psql -U postgres -d ordersdb

# Run the script
\i d:/Ki9_DoAn/21th11Sketcnote/SketchNote_BE/order-service/src/main/resources/seed_dashboard_data.sql
```

### Option 2: Using DBeaver / pgAdmin

1. Open DBeaver/pgAdmin
2. Connect to `ordersdb` database
3. Open SQL Editor
4. Copy and paste the content from `seed_dashboard_data.sql`
5. Execute the script

### Option 3: Using Docker

```bash
# If using Docker container
docker exec -i <postgres_container_name> psql -U postgres -d ordersdb < seed_dashboard_data.sql
```

---

## Important Notes

### Before Running

> [!WARNING]
> Script này sẽ INSERT data mới. Nếu bạn đã có data, nó sẽ thêm vào chứ không xóa data cũ.

### Designer ID

Script mặc định sử dụng `designer_id = 1`. Nếu bạn muốn dùng designer ID khác:

1. Mở file `seed_dashboard_data.sql`
2. Tìm dòng: `(1, 'Modern UI Kit', ...`
3. Thay `1` bằng designer ID của bạn
4. Thay tất cả các dòng INSERT template

### User IDs

Script tạo orders với `user_id` từ 1-100 (random). Đảm bảo users này tồn tại trong database hoặc adjust logic nếu cần.

---

## Verification Queries

### Check Total Orders by Year

```sql
SELECT 
    EXTRACT(YEAR FROM issue_date) as year,
    COUNT(*) as total_orders,
    SUM(total_amount) as total_revenue
FROM orders
WHERE payment_status = 'PAID' 
  AND order_status IN ('CONFIRMED', 'DELIVERED')
GROUP BY EXTRACT(YEAR FROM issue_date)
ORDER BY year;
```

### Check Monthly Revenue for a Specific Year

```sql
SELECT 
    TO_CHAR(issue_date, 'YYYY-MM') as month,
    COUNT(*) as total_orders,
    SUM(total_amount) as total_revenue
FROM orders
WHERE payment_status = 'PAID' 
  AND order_status IN ('CONFIRMED', 'DELIVERED')
  AND EXTRACT(YEAR FROM issue_date) = 2024
GROUP BY TO_CHAR(issue_date, 'YYYY-MM')
ORDER BY month;
```

### Check Top Templates

```sql
SELECT 
    rt.template_id,
    rt.name,
    COUNT(*) as sold_count,
    SUM(od.subtotal_amount) as revenue
FROM order_details od
JOIN orders o ON od.order_id = o.order_id
JOIN resource_template rt ON od.resource_template_id = rt.template_id
WHERE o.payment_status = 'PAID'
  AND o.order_status IN ('CONFIRMED', 'DELIVERED')
  AND rt.designer_id = 1
GROUP BY rt.template_id, rt.name
ORDER BY sold_count DESC
LIMIT 10;
```

---

## Expected Dashboard Results

### Yearly Revenue (2015-2025)

Approximate values (will vary due to random factors):

| Year | Orders | Revenue (VND) |
|------|--------|---------------|
| 2015 | ~600 | ~150M |
| 2016 | ~700 | ~175M |
| 2017 | ~800 | ~200M |
| 2018 | ~950 | ~240M |
| 2019 | ~1,100 | ~280M |
| 2020 | ~1,250 | ~320M |
| 2021 | ~1,450 | ~370M |
| 2022 | ~1,650 | ~420M |
| 2023 | ~1,900 | ~480M |
| 2024 | ~2,200 | ~560M |
| 2025 | ~2,000 | ~510M (partial year) |

### Revenue Pattern

```
Revenue Growth Chart (Approximate):

2025 ████████████████████████░░  510M (Jan-Nov only)
2024 ███████████████████████████  560M
2023 ████████████████████████░░  480M
2022 ██████████████████████░░░░  420M
2021 ███████████████████░░░░░░░  370M
2020 ████████████████░░░░░░░░░░  320M
2019 ██████████████░░░░░░░░░░░░  280M
2018 ████████████░░░░░░░░░░░░░░  240M
2017 ██████████░░░░░░░░░░░░░░░░  200M
2016 ████████░░░░░░░░░░░░░░░░░░  175M
2015 ██████░░░░░░░░░░░░░░░░░░░░  150M
```

---

## Customization

### Adjust Growth Rate

Tìm dòng trong script:
```sql
v_growth_factor := 1.0 + ((v_year - 2015) * 0.15);
```

Thay `0.15` (15% growth) bằng giá trị khác:
- `0.10` = 10% growth per year
- `0.20` = 20% growth per year

### Adjust Seasonal Variations

Tìm section:
```sql
v_seasonal_factor := CASE
    WHEN v_month IN (1, 2) THEN 0.7  -- Adjust này
    WHEN v_month IN (3, 4, 5) THEN 0.9
    ...
END;
```

### Adjust Base Orders

Tìm dòng:
```sql
v_base_orders := (15 * v_growth_factor * v_seasonal_factor)::INT;
```

Thay `15` bằng số khác để tăng/giảm số lượng orders.

---

## Troubleshooting

### Error: "designer_id does not exist"

Tạo designer trước hoặc thay `designer_id = 1` trong script.

### Error: "user_id does not exist"

Nếu có foreign key constraint, tạo users trước hoặc disable constraint tạm thời.

### Script runs too slow

Giảm `v_base_orders` hoặc giảm range năm (ví dụ: 2020-2025 thay vì 2015-2025).

### Want to clear old data first

```sql
-- CẢNH BÁO: Xóa tất cả orders và order_details!
DELETE FROM order_details;
DELETE FROM orders;
DELETE FROM resource_template WHERE designer_id = 1;
```
