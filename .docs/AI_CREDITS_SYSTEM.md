# Hệ Thống AI Credits

## Tổng quan

Hệ thống credit được xây dựng để giới hạn người dùng sử dụng các chức năng AI, bao gồm:
- **AI Image Generation** (Imagen 3.0)
- **AI Background Removal**

## Cấu hình Credit

### Chi phí sử dụng
- **Generate Image**: 10 credits/ảnh
- **Remove Background**: 5 credits/lần

### Credit miễn phí
- User mới sẽ nhận **50 credits miễn phí** khi đăng ký

## API Endpoints

### 1. Xem số credit còn lại
```http
GET /api/credits/balance
Authorization: Bearer {access_token}
```

**Response:**
```json
{
  "code": 200,
  "message": "Credit balance retrieved successfully",
  "data": {
    "userId": 1,
    "email": "user@example.com",
    "currentBalance": 50,
    "totalPurchased": 100,
    "totalUsed": 50,
    "usageCount": 5
  }
}
```

### 2. Mua credit
```http
POST /api/credits/purchase
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "amount": 100,
  "paymentMethod": "wallet"
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Successfully purchased 100 credits",
  "data": {
    "userId": 1,
    "email": "user@example.com",
    "currentBalance": 150,
    "totalPurchased": 200,
    "totalUsed": 50,
    "usageCount": 5
  }
}
```

**Lưu ý:** Số credit tối thiểu phải mua là **100 credits**

### 3. Kiểm tra credit
```http
GET /api/credits/check?amount=10
Authorization: Bearer {access_token}
```

**Response:**
```json
{
  "code": 200,
  "message": "Sufficient credits",
  "data": true
}
```

### 4. Xem lịch sử giao dịch
```http
GET /api/credits/history?page=0&size=10
Authorization: Bearer {access_token}
```

**Response:**
```json
{
  "code": 200,
  "message": "Credit history retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "type": "USAGE",
        "amount": -10,
        "balanceAfter": 40,
        "description": "Generated 2 image(s)",
        "referenceId": "https://bucket.s3.amazonaws.com/image.png",
        "createdAt": "2025-12-02T07:00:00"
      },
      {
        "id": 2,
        "type": "PURCHASE",
        "amount": 100,
        "balanceAfter": 50,
        "description": "Purchased 100 credits",
        "referenceId": null,
        "createdAt": "2025-12-01T10:00:00"
      }
    ],
    "pageable": {...},
    "totalElements": 10,
    "totalPages": 1
  }
}
```

## Sử dụng AI Features

### Generate Image
```http
POST /api/images/generate
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "prompt": "A beautiful sunset over mountains",
  "isIcon": false
}
```

**Lưu ý:** 
- Hệ thống sẽ tự động kiểm tra credit trước khi generate
- Nếu không đủ credit, sẽ trả về lỗi `INSUFFICIENT_CREDITS` (HTTP 400)
- Sau khi generate thành công, credit sẽ tự động bị trừ

### Remove Background
```http
POST /api/images/remove-background
Authorization: Bearer {access_token}
Content-Type: multipart/form-data

file: <image_file>
```

**Lưu ý:**
- Tương tự generate image, hệ thống sẽ kiểm tra và trừ credit tự động
- Nếu không đủ credit, sẽ trả về HTTP 402 (Payment Required)

## Error Codes

| Code | Message | Description |
|------|---------|-------------|
| 400 | INSUFFICIENT_CREDITS | Không đủ credit để thực hiện thao tác |
| 400 | INVALID_CREDIT_AMOUNT | Số lượng credit không hợp lệ |
| 400 | MINIMUM_PURCHASE_NOT_MET | Số credit mua phải tối thiểu 100 |
| 500 | CREDIT_TRANSACTION_FAILED | Giao dịch credit thất bại |
| 500 | CREDIT_CHECK_FAILED | Không thể kiểm tra số dư credit |

## Database Schema

### Table: `users`
```sql
ALTER TABLE users ADD COLUMN ai_credits INTEGER NOT NULL DEFAULT 0;
```

### Table: `credit_transactions`
```sql
CREATE TABLE credit_transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,  -- PURCHASE, USAGE, REFUND, BONUS, INITIAL_BONUS
    amount INTEGER NOT NULL,     -- Dương: thêm, Âm: trừ
    balance_after INTEGER NOT NULL,
    description VARCHAR(255),
    reference_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_credit_user FOREIGN KEY (user_id) REFERENCES users(id)
);
```

## Luồng hoạt động

### 1. User mới đăng ký
```
1. User đăng ký tài khoản
2. AuthenticationService tạo User trong DB
3. UserCreatedEvent được publish
4. UserCreatedEventListener nhận event
5. CreditService.grantInitialCredits() được gọi
6. User nhận 50 credits miễn phí
7. CreditTransaction được tạo với type=INITIAL_BONUS
```

### 2. User sử dụng AI Generate Image
```
1. User gọi POST /api/images/generate
2. ImageGenerationController extract userId từ JWT
3. Gọi CreditClient.checkCredits(userId, 10)
4. Nếu đủ credit:
   - Gọi ImageGenerationService.generateAndUploadImage()
   - Gọi CreditClient.useCredits() để trừ 10 credits
   - Trả về ảnh đã generate
5. Nếu không đủ credit:
   - Throw AppException(INSUFFICIENT_CREDITS)
```

### 3. User mua thêm credit
```
1. User gọi POST /api/credits/purchase với amount=100
2. CreditService kiểm tra amount >= 100
3. Tính tổng tiền = amount * CREDIT_PRICE (1000 VNĐ/credit)
4. TODO: Tích hợp payment gateway
5. Cập nhật user.aiCredits += amount
6. Tạo CreditTransaction với type=PURCHASE
7. Trả về thông tin credit balance mới
```

## Tích hợp với Payment Gateway (TODO)

Hiện tại hệ thống chưa tích hợp payment gateway. Để hoàn thiện, cần:

1. **Tích hợp VNPay/Momo/Stripe**
   - Tạo payment order
   - Redirect user đến payment gateway
   - Xử lý callback sau khi thanh toán

2. **Cập nhật CreditService.purchaseCredits()**
   ```java
   // Thay vì giả sử thanh toán thành công
   // Cần gọi payment gateway API
   PaymentResponse payment = paymentGateway.createPayment(totalAmount);
   if (!payment.isSuccess()) {
       throw new AppException(ErrorCode.PAYMENT_FAILED);
   }
   ```

3. **Xử lý webhook từ payment gateway**
   - Tạo PaymentWebhookController
   - Verify signature
   - Cập nhật credit sau khi thanh toán thành công

## Testing

### Test với Postman

1. **Đăng ký user mới và kiểm tra credit**
```bash
# 1. Register
POST http://localhost:8080/api/auth/register
{
  "email": "test@example.com",
  "password": "password123",
  "firstName": "Test",
  "lastName": "User"
}

# 2. Login
POST http://localhost:8080/api/auth/login
{
  "email": "test@example.com",
  "password": "password123"
}

# 3. Check credit balance (should be 50)
GET http://localhost:8080/api/credits/balance
Authorization: Bearer {access_token}
```

2. **Test generate image**
```bash
POST http://localhost:8080/api/images/generate
Authorization: Bearer {access_token}
{
  "prompt": "A cat sitting on a chair",
  "isIcon": false
}

# Check balance again (should be 40)
GET http://localhost:8080/api/credits/balance
Authorization: Bearer {access_token}
```

3. **Test insufficient credits**
```bash
# Use credits until balance < 10
# Then try to generate image
POST http://localhost:8080/api/images/generate
Authorization: Bearer {access_token}
{
  "prompt": "Another image",
  "isIcon": false
}

# Should return 400 INSUFFICIENT_CREDITS
```

## Monitoring & Analytics

Có thể thêm các query để theo dõi:

```sql
-- Tổng credit đã sử dụng trong hệ thống
SELECT SUM(ABS(amount)) as total_credits_used
FROM credit_transactions
WHERE type = 'USAGE';

-- Top users sử dụng nhiều credit nhất
SELECT u.email, SUM(ABS(ct.amount)) as total_used
FROM credit_transactions ct
JOIN users u ON ct.user_id = u.id
WHERE ct.type = 'USAGE'
GROUP BY u.id, u.email
ORDER BY total_used DESC
LIMIT 10;

-- Thống kê theo ngày
SELECT DATE(created_at) as date,
       COUNT(*) as transactions,
       SUM(CASE WHEN type = 'USAGE' THEN ABS(amount) ELSE 0 END) as credits_used,
       SUM(CASE WHEN type = 'PURCHASE' THEN amount ELSE 0 END) as credits_purchased
FROM credit_transactions
GROUP BY DATE(created_at)
ORDER BY date DESC;
```

## Cấu hình

Có thể thêm vào `application.yml`:

```yaml
credit:
  initial-free-credits: 50
  price-per-credit: 1000  # VNĐ
  minimum-purchase: 100
  costs:
    image-generation: 10
    background-removal: 5
```

## Roadmap

- [ ] Tích hợp payment gateway (VNPay/Momo)
- [ ] Thêm gói subscription với unlimited credits
- [ ] Thêm promotion/bonus credits
- [ ] Thêm referral system (mời bạn nhận credits)
- [ ] Thêm daily free credits cho active users
- [ ] Admin dashboard để quản lý credits
- [ ] Email notification khi credit sắp hết
