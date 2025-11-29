# Hướng Dẫn Build và Test

## Build Services

### 1. Build Order Service
```bash
cd order-service
mvn clean install -DskipTests
```

### 2. Build Identity Service
```bash
cd identity-service
mvn clean install -DskipTests
```

## Test API

### 1. Test GET All Templates với Statistics
```bash
curl -X GET "http://localhost:8080/api/orders/template?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Response**:
```json
{
  "code": 200,
  "message": "Fetched templates",
  "result": {
    "content": [
      {
        "resourceTemplateId": 1,
        "name": "Template Name",
        "price": 29.99,
        "purchaseCount": 10,
        "feedbackCount": 5,
        "averageRating": 4.5,
        ...
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 50
  }
}
```

### 2. Test GET Template by ID với Statistics
```bash
curl -X GET "http://localhost:8080/api/orders/template/1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 3. Test Feedback Stats Endpoint (Identity Service)
```bash
curl -X GET "http://localhost:8080/api/feedback/resource/1/stats"
```

**Expected Response**:
```json
{
  "code": 200,
  "message": "Resource feedback stats retrieved successfully",
  "result": {
    "totalFeedbacks": 5,
    "averageRating": 4.5
  }
}
```

## Verify Database

### Check Purchase Count
```sql
SELECT 
    resource_template_id,
    COUNT(*) as purchase_count
FROM orders
WHERE payment_status = 'PAID' 
  AND order_status = 'SUCCESS'
  AND resource_template_id IS NOT NULL
GROUP BY resource_template_id;
```

### Check Feedback Stats
```sql
SELECT 
    resourceId,
    COUNT(*) as feedback_count,
    AVG(rating) as average_rating
FROM feedbacks
WHERE resourceId IS NOT NULL
GROUP BY resourceId;
```

## Troubleshooting

### Issue: Feedback stats trả về 0/null
**Possible causes**:
1. Identity service chưa chạy
2. Feign client configuration chưa đúng
3. Chưa có feedback nào cho template

**Solution**:
- Kiểm tra identity service đang chạy
- Kiểm tra logs của order-service để xem có error khi gọi identity-service không
- Tạo feedback test data

### Issue: Purchase count = 0 nhưng đã có orders
**Possible causes**:
1. Order status không phải SUCCESS
2. Payment status không phải PAID
3. resourceTemplateId null trong order

**Solution**:
- Kiểm tra database với query ở trên
- Đảm bảo orders có đúng status

## Create Test Data

### 1. Create Orders
```sql
INSERT INTO orders (user_id, resource_template_id, total_amount, payment_status, order_status, created_at, updated_at)
VALUES 
(1, 1, 29.99, 'PAID', 'SUCCESS', NOW(), NOW()),
(2, 1, 29.99, 'PAID', 'SUCCESS', NOW(), NOW()),
(3, 1, 29.99, 'PAID', 'SUCCESS', NOW(), NOW());
```

### 2. Create Feedbacks (via API)
```bash
curl -X POST "http://localhost:8080/api/feedback/resource" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "resourceId": 1,
    "rating": 5,
    "comment": "Great template!"
  }'
```

## Performance Testing

### Test with many templates
```bash
# Get 100 templates at once
curl -X GET "http://localhost:8080/api/orders/template?page=0&size=100" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -w "\nTime: %{time_total}s\n"
```

Monitor response time and consider adding caching if needed.
