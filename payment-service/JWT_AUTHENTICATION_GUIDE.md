# JWT Authentication Implementation Guide

## Tổng quan
Đã thêm JWT authentication vào payment service để thay thế các hàm `getById` bằng cách lấy ID từ token khi đăng nhập.

## Các thay đổi chính

### 1. Dependencies đã thêm
- `spring-boot-starter-security`
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson` (JWT libraries)

### 2. Cấu hình JWT
- **JwtUtil**: Utility class để parse và validate JWT tokens
- **JwtAuthenticationFilter**: Filter để xử lý authentication từ request headers
- **SecurityConfig**: Spring Security configuration
- **SecurityContextUtil**: Utility để lấy userId từ SecurityContext

### 3. Controllers đã cập nhật

#### WalletController
- `GET /api/wallet/me` - Lấy ví của user hiện tại từ JWT token
- Giữ lại `GET /api/wallet/{userId}` cho backward compatibility

#### PaymentController  
- `POST /api/payment/deposit` - Tạo payment link cho user hiện tại (không cần truyền walletId)

#### TransactionController
- `GET /api/payment/transactions/my` - Lấy transactions của user hiện tại
- `GET /api/payment/transactions/filter` - Filter transactions (có thể không truyền walletId)

### 4. Services đã cập nhật
- **PayOSServiceImpl**: Thêm method `createPaymentLinkByUserId()`
- **TransactionServiceImp**: Thêm method `getTransactionsByUserId()`

## Cách sử dụng

### 1. Authentication
Client cần gửi JWT token trong header:
```
Authorization: Bearer <jwt_token>
```

### 2. Endpoints mới
- `GET /api/wallet/me` - Lấy ví của user hiện tại
- `POST /api/payment/deposit?amount=100000` - Tạo payment link
- `GET /api/payment/transactions/my` - Lấy transactions của user

### 3. Endpoints không cần authentication
- `POST /api/payment/callback` - PayOS callback
- `POST /api/wallet/create` - Tạo ví mới

## Cấu hình JWT
Trong `application.yml`:
```yaml
jwt:
  secret: mySecretKeyForJWTTokenGenerationAndValidation123456789
  expiration: 86400000  # 24 hours
```

## Lưu ý quan trọng
1. JWT token phải chứa `userId` trong claims
2. Token phải có format: `{"sub": "username", "userId": 123}`
3. Các endpoint cũ vẫn hoạt động để đảm bảo backward compatibility
4. Security configuration cho phép một số endpoints không cần authentication

## Testing
Để test, cần:
1. Tạo JWT token với userId trong claims
2. Gửi request với header `Authorization: Bearer <token>`
3. Service sẽ tự động lấy userId từ token thay vì yêu cầu truyền tham số
