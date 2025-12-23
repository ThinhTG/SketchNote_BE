# Password Storage & Google OAuth Login Implementation

## Overview
Hệ thống đã được cập nhật để hỗ trợ lưu trữ mật khẩu và cho phép người dùng đã đăng ký bằng email/password có thể đăng nhập bằng Google OAuth.

## Các thay đổi chính

### 1. **User Entity** (`User.java`)
- Thêm field `password` để lưu mật khẩu đã mã hóa
- Sử dụng `@JsonIgnore` để bảo mật, không trả về password trong API response

### 2. **Password Encryption** (`PasswordEncryptionUtil.java`)
- Sử dụng **AES encryption** (mã hóa 2 chiều) thay vì BCrypt (băm 1 chiều)
- Lý do: Cần giải mã password để đăng nhập vào Keycloak khi user dùng Google OAuth
- Encryption key được cấu hình trong `application.yaml`

### 3. **Authentication Flow**

#### **Register Flow (Email/Password)**
```
1. User đăng ký với email + password
2. Tạo user trong Keycloak với password gốc (Keycloak tự băm)
3. Mã hóa password bằng AES và lưu vào DB
4. Gửi email xác thực
```

#### **Google OAuth Login Flow**
```
1. User đăng nhập bằng Google ID Token
2. Verify token và lấy thông tin email
3. Kiểm tra email có tồn tại trong DB không?
   
   a) Nếu ĐÃ TỒN TẠI (đã đăng ký trước đó):
      - Giải mã password từ DB
      - Dùng password đó để login vào Keycloak (grant_type=password)
      - Trả về access_token + refresh_token
   
   b) Nếu CHƯA TỒN TẠI (user mới):
      - Tạo user mới trong Keycloak với random password
      - Lưu user vào DB (không lưu password vì là Google user)
      - Login vào Keycloak với random password
      - Trả về access_token + refresh_token
```

### 4. **Database Migration** (`V8__add_password_column.sql`)
- Thêm column `password VARCHAR(500)` vào bảng `users`
- Nullable vì Google OAuth users không có password

### 5. **Security Configuration** (`SecurityConfig.java`)
- Thêm `PasswordEncoder` bean (BCryptPasswordEncoder)
- Mặc dù không dùng để mã hóa password chính, nhưng có thể dùng cho các mục đích khác

## Configuration

### Application.yaml
```yaml
security:
  password:
    encryption-key: "SketchNote2025!!"  # 16 ký tự cho AES-128
```

**⚠️ QUAN TRỌNG**: Trong production, phải:
1. Thay đổi encryption key thành giá trị bí mật
2. Lưu key trong environment variable hoặc secret manager
3. Không commit key vào Git

## Security Considerations

### Tại sao dùng AES thay vì BCrypt?
- **BCrypt**: Băm 1 chiều, không thể giải mã → Không thể lấy lại password gốc
- **AES**: Mã hóa 2 chiều, có thể giải mã → Cần thiết để login vào Keycloak

### Có an toàn không?
- ✅ Password được mã hóa AES-128 trong DB
- ✅ Encryption key được bảo vệ trong config
- ✅ Password không bao giờ được trả về trong API response (`@JsonIgnore`)
- ⚠️ Cần bảo vệ encryption key cẩn thận
- ⚠️ Trong production nên dùng HSM hoặc Key Management Service

### Best Practices
1. Sử dụng HTTPS cho tất cả API calls
2. Rotate encryption key định kỳ
3. Monitor access logs cho bất thường
4. Implement rate limiting cho login endpoints

## Testing

### Test Register
```bash
POST /api/auth/register
{
  "email": "test@example.com",
  "password": "password123",
  "firstName": "Test",
  "lastName": "User"
}
```

### Test Google Login (Existing User)
```bash
POST /api/auth/login-google-mobile
{
  "idToken": "google_id_token_here"
}
```

## Error Codes
- `EMAIL_EXISTED`: Email đã tồn tại khi register
- `USER_INACTIVE`: User bị vô hiệu hóa
- `INVALID_TOKEN`: Google ID token không hợp lệ
- `EMAIL_NOT_VERIFIED`: Email chưa được xác thực

## Migration Guide

### Existing Users
- Users đã tồn tại trong DB sẽ có `password = NULL`
- Khi họ đăng nhập lần đầu bằng Google OAuth, hệ thống sẽ tạo random password
- Nếu họ muốn đăng nhập bằng email/password, cần reset password

### New Users
- Register bằng email/password: Password được mã hóa và lưu
- Register bằng Google OAuth: Không lưu password (hoặc lưu random password)

## Troubleshooting

### Lỗi "Error encrypting password"
- Kiểm tra encryption key có đúng 16 ký tự không
- Kiểm tra config trong application.yaml

### Lỗi "Error decrypting password"
- Password trong DB bị corrupt
- Encryption key đã thay đổi
- Giải pháp: User cần reset password

### Google Login không hoạt động
- Kiểm tra Google client IDs trong config
- Verify Google ID token có hợp lệ không
- Kiểm tra Keycloak có hoạt động không
