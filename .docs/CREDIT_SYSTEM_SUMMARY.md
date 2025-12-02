# Tổng kết: Hệ thống AI Credits

## 📋 Tổng quan

Đã xây dựng hoàn chỉnh hệ thống credit để giới hạn người dùng sử dụng các chức năng AI, bao gồm:

✅ **Mua credit** (gói mặc định 100 credits)  
✅ **Xem số credit còn lại**  
✅ **Trừ credit khi sử dụng AI Generate Image**  
✅ **Theo dõi tổng credit đã xài**  
✅ **Tặng 50 credits miễn phí cho user mới**

---

## 📁 Files đã tạo/sửa

### **Identity Service** (Backend - Credit Management)

#### **Models & Entities**
1. ✅ `CreditTransaction.java` - Entity lưu lịch sử giao dịch credit
2. ✅ `User.java` - Thêm field `aiCredits` và relationship với `CreditTransaction`

#### **Enums**
3. ✅ `CreditTransactionType.java` - Enum định nghĩa loại giao dịch (PURCHASE, USAGE, REFUND, BONUS, INITIAL_BONUS)

#### **DTOs**
4. ✅ `PurchaseCreditRequest.java` - Request DTO cho việc mua credit
5. ✅ `UseCreditRequest.java` - Request DTO cho việc sử dụng credit
6. ✅ `CreditBalanceResponse.java` - Response DTO cho thông tin credit balance
7. ✅ `CreditTransactionResponse.java` - Response DTO cho lịch sử giao dịch

#### **Repository**
8. ✅ `CreditTransactionRepository.java` - Repository với các query methods

#### **Service**
9. ✅ `ICreditService.java` - Interface định nghĩa các methods
10. ✅ `CreditService.java` - Implementation với logic nghiệp vụ

#### **Controller**
11. ✅ `CreditController.java` - REST API endpoints cho credit

#### **Events**
12. ✅ `UserCreatedEvent.java` - Event được publish khi user mới được tạo
13. ✅ `UserCreatedEventListener.java` - Listener tự động tặng credit cho user mới

#### **Exception**
14. ✅ `ErrorCode.java` - Thêm error codes cho credit (INSUFFICIENT_CREDITS, INVALID_CREDIT_AMOUNT, etc.)

#### **Database Migration**
15. ✅ `V5__add_ai_credits_system.sql` - Migration script để tạo bảng và columns

---

### **Project Service** (Backend - AI Features)

#### **Client**
16. ✅ `CreditClient.java` - Feign Client để gọi Credit API từ Identity Service

#### **DTOs**
17. ✅ `UseCreditRequest.java` - Request DTO (copy từ identity-service)
18. ✅ `CreditBalanceResponse.java` - Response DTO (copy từ identity-service)

#### **Controller**
19. ✅ `ImageGenerationController.java` - Thêm logic kiểm tra và trừ credit

#### **Exception**
20. ✅ `ErrorCode.java` - Thêm error codes (INSUFFICIENT_CREDITS, CREDIT_CHECK_FAILED)

---

### **Documentation**

21. ✅ `AI_CREDITS_SYSTEM.md` - Tài liệu chi tiết về hệ thống credit
22. ✅ `INTEGRATION_GUIDE.md` - Hướng dẫn tích hợp UserCreatedEvent

---

## 🔧 Các chức năng chính

### 1. **Mua Credit**
- Endpoint: `POST /api/credits/purchase`
- Số lượng tối thiểu: 100 credits
- Giá: 1,000 VNĐ/credit (có thể config)
- Tự động tạo transaction record

### 2. **Xem Credit Balance**
- Endpoint: `GET /api/credits/balance`
- Hiển thị:
  - Số credit hiện tại
  - Tổng credit đã mua
  - Tổng credit đã sử dụng
  - Số lần sử dụng AI

### 3. **Lịch sử giao dịch**
- Endpoint: `GET /api/credits/history?page=0&size=10`
- Hỗ trợ pagination
- Hiển thị đầy đủ thông tin mỗi transaction

### 4. **Kiểm tra Credit**
- Endpoint: `GET /api/credits/check?amount=10`
- Trả về true/false

### 5. **Sử dụng Credit (Internal API)**
- Endpoint: `POST /api/credits/use`
- Được gọi từ project-service qua Feign Client
- Tự động trừ credit và tạo transaction

---

## 💰 Chi phí sử dụng AI

| Chức năng | Chi phí |
|-----------|---------|
| Generate Image | 10 credits/ảnh |
| Remove Background | 5 credits/lần |

---

## 🎁 Credit miễn phí

- **User mới**: 50 credits miễn phí khi đăng ký
- Tự động được tặng thông qua `UserCreatedEvent`

---

## 🔄 Luồng hoạt động

### **User đăng ký mới**
```
1. User đăng ký → AuthenticationService.register()
2. User được tạo trong DB
3. Wallet được tạo
4. UserCreatedEvent được publish
5. UserCreatedEventListener nhận event (async)
6. CreditService.grantInitialCredits(userId, 50)
7. User nhận 50 credits miễn phí
```

### **User sử dụng AI Generate Image**
```
1. POST /api/images/generate
2. Extract userId từ JWT token
3. Check credit: CreditClient.checkCredits(userId, 10)
4. Nếu đủ credit:
   - Generate image
   - Trừ credit: CreditClient.useCredits()
   - Return image URLs
5. Nếu không đủ:
   - Throw INSUFFICIENT_CREDITS error
```

---

## 📊 Database Schema

### **Table: users**
```sql
ALTER TABLE users ADD COLUMN ai_credits INTEGER NOT NULL DEFAULT 0;
```

### **Table: credit_transactions**
```sql
CREATE TABLE credit_transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    amount INTEGER NOT NULL,
    balance_after INTEGER NOT NULL,
    description VARCHAR(255),
    reference_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_credit_user FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

## ⚙️ Cấu hình cần thiết

### **1. Enable Async Processing**

Tạo file `AsyncConfiguration.java`:

```java
@Configuration
@EnableAsync
public class AsyncConfiguration {
}
```

### **2. Tích hợp UserCreatedEvent**

Cần thêm code vào `AuthenticationService.java`:

1. Inject `ApplicationEventPublisher`
2. Publish event sau khi tạo user trong `register()` và `loginWithGoogle()`

Chi tiết xem file: `.docs/INTEGRATION_GUIDE.md`

---

## 🧪 Testing

### **Test Flow**

1. **Đăng ký user mới**
```bash
POST /api/auth/register
{
  "email": "test@example.com",
  "password": "password123",
  "firstName": "Test",
  "lastName": "User"
}
```

2. **Login và lấy token**
```bash
POST /api/auth/login
{
  "email": "test@example.com",
  "password": "password123"
}
```

3. **Kiểm tra credit (should be 50)**
```bash
GET /api/credits/balance
Authorization: Bearer {token}
```

4. **Generate image (cost: 10 credits)**
```bash
POST /api/images/generate
Authorization: Bearer {token}
{
  "prompt": "A beautiful sunset",
  "isIcon": false
}
```

5. **Kiểm tra lại credit (should be 40)**
```bash
GET /api/credits/balance
Authorization: Bearer {token}
```

6. **Xem lịch sử**
```bash
GET /api/credits/history?page=0&size=10
Authorization: Bearer {token}
```

---

## 🚀 Deployment

### **1. Run Migration**
Migration sẽ tự động chạy khi start application (Flyway)

### **2. Restart Services**
```bash
# Restart identity-service
# Restart project-service
```

### **3. Verify**
- Check logs để đảm bảo không có errors
- Test các endpoints

---

## 📝 TODO / Future Enhancements

- [ ] Tích hợp payment gateway (VNPay/Momo/Stripe)
- [ ] Thêm gói subscription với unlimited credits
- [ ] Promotion/Bonus credits system
- [ ] Referral program (mời bạn nhận credits)
- [ ] Daily free credits cho active users
- [ ] Admin dashboard để quản lý credits
- [ ] Email notification khi credit sắp hết
- [ ] Credit expiration (credits hết hạn sau X ngày)

---

## 🐛 Troubleshooting

### **User không nhận được 50 credits miễn phí**

1. Check logs:
```
Published UserCreatedEvent for user: {userId}
Handling UserCreatedEvent for user: {userId}
Successfully granted 50 initial credits to user: {userId}
```

2. Verify `@EnableAsync` đã được config
3. Check database: `SELECT * FROM credit_transactions WHERE type = 'INITIAL_BONUS'`

### **Credit không bị trừ khi generate image**

1. Check Feign Client configuration
2. Verify JWT token có claim `userId`
3. Check logs trong `ImageGenerationController`

### **Error: INSUFFICIENT_CREDITS**

- User không đủ credit
- Cần mua thêm credit hoặc chờ promotion

---

## 📚 Tài liệu tham khảo

- `AI_CREDITS_SYSTEM.md` - Chi tiết về hệ thống
- `INTEGRATION_GUIDE.md` - Hướng dẫn tích hợp event
- API Documentation: Xem Swagger UI khi chạy application

---

## ✅ Checklist triển khai

- [x] Tạo models và entities
- [x] Tạo repositories
- [x] Tạo services và business logic
- [x] Tạo REST API controllers
- [x] Tạo Feign clients
- [x] Tích hợp với AI features
- [x] Tạo database migration
- [x] Viết documentation
- [ ] Tích hợp UserCreatedEvent vào AuthenticationService (cần làm thủ công)
- [ ] Enable Async configuration
- [ ] Test toàn bộ flow
- [ ] Deploy lên production

---

## 👨‍💻 Hỗ trợ

Nếu có vấn đề, check:
1. Logs của cả 2 services (identity-service, project-service)
2. Database để verify data
3. Network connectivity giữa các services
4. JWT token configuration

---

**Ngày tạo**: 2025-12-02  
**Version**: 1.0  
**Status**: ✅ Ready for Integration
