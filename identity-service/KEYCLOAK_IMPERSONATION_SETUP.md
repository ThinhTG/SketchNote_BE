# Keycloak Impersonation Setup Guide

## Tổng quan

Để cho phép existing users login bằng Google OAuth mà không thay đổi password của họ, chúng ta sử dụng **Keycloak Impersonation**.

## Impersonation là gì?

**Impersonation** cho phép admin (service account) tạo token cho user mà không cần biết password của họ.

### Ưu điểm:
- ✅ **Bảo mật**: Không cần lưu password vào database
- ✅ **Chuẩn Keycloak**: Sử dụng tính năng có sẵn của Keycloak
- ✅ **Linh hoạt**: User có thể login bằng cả email/password VÀ Google OAuth

## Cách hoạt động

### User mới (tạo từ Google OAuth):
1. User login Google lần đầu
2. Hệ thống tạo user trong Keycloak với random password
3. Sử dụng password grant để lấy token
4. User được tạo trong database

### User cũ (đã tồn tại):
1. User login Google lần 2 (hoặc user đã register trước đó)
2. Hệ thống nhận diện user đã tồn tại (theo email)
3. **KHÔNG** thay đổi password của user
4. Sử dụng **impersonation** để lấy token
5. Admin service "đóng giả" user để lấy token

## Cấu hình Keycloak

### Bước 1: Enable Impersonation cho Service Account

1. Đăng nhập vào Keycloak Admin Console
2. Chọn Realm của bạn (ví dụ: `sketchnote`)
3. Vào **Clients** → Chọn client của bạn (ví dụ: `sketchnote-client`)

### Bước 2: Cấp quyền Impersonation

1. Vào tab **Service Account Roles**
2. Trong **Client Roles**, chọn `realm-management`
3. Thêm các roles sau:
   - `impersonation` ✅ (Quan trọng nhất!)
   - `view-users`
   - `manage-users` (optional, nếu cần)

### Bước 3: Enable Impersonation trong Realm Settings

1. Vào **Realm Settings** → **Security Defenses**
2. Đảm bảo **Impersonation** không bị disable

### Bước 4: Test Impersonation

Sử dụng Keycloak Admin API để test:

```bash
# 1. Get admin token
curl -X POST "http://localhost:8080/realms/sketchnote/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=sketchnote-client" \
  -d "client_secret=YOUR_CLIENT_SECRET"

# 2. Impersonate user
curl -X POST "http://localhost:8080/admin/realms/sketchnote/users/{userId}/impersonation" \
  -H "Authorization: Bearer {admin_token}"
```

## Code Implementation

### IdentityClient.java
```java
@PostMapping("/admin/realms/${idp.client-id}/users/{userId}/impersonation")
LoginExchangeResponse impersonateUser(
    @RequestHeader("authorization") String token,
    @PathVariable("userId") String userId
);
```

### AuthenticationService.java
```java
// Get admin token
TokenExchangeResponse adminToken = identityClient.exchangeClientToken(...);

// Impersonate user
LoginExchangeResponse tokenResponse = identityClient.impersonateUser(
    "Bearer " + adminToken.getAccessToken(),
    user.getKeycloakId()
);
```

## Flow Diagram

```
User Login với Google
        ↓
Email đã tồn tại?
        ↓
    YES → Existing User
        ↓
    Lấy admin token
        ↓
    Impersonate user
        ↓
    Trả về user token
        ↓
    User login thành công
    (Password KHÔNG bị thay đổi!)
```

## Troubleshooting

### Lỗi: "Forbidden" khi impersonate
**Nguyên nhân**: Service account chưa có quyền `impersonation`
**Giải pháp**: Thêm role `impersonation` trong Service Account Roles

### Lỗi: "User not found"
**Nguyên nhân**: User chưa có `keycloakId` trong database
**Giải pháp**: Đảm bảo user đã được tạo trong Keycloak

### Lỗi: "Invalid token"
**Nguyên nhân**: Admin token hết hạn hoặc không hợp lệ
**Giải pháp**: Lấy admin token mới trước khi impersonate

## Security Considerations

1. **Chỉ service account mới có quyền impersonation**
   - User thường KHÔNG được phép impersonate
   - Client secret phải được bảo mật

2. **Log tất cả impersonation events**
   - Keycloak tự động log
   - Có thể audit trong Admin Console

3. **Giới hạn scope của impersonation**
   - Chỉ impersonate khi cần thiết (Google OAuth login)
   - Không lạm dụng cho mục đích khác

## Alternative Solutions

Nếu không muốn dùng impersonation, có thể:

1. **Chỉ hỗ trợ Google login cho user mới**
   - User cũ phải dùng email/password
   - Đơn giản nhưng UX không tốt

2. **Yêu cầu user link account**
   - User phải xác nhận link Google với account hiện tại
   - Phức tạp hơn nhưng an toàn hơn

3. **Sử dụng Keycloak Identity Provider**
   - Configure Google làm Identity Provider trong Keycloak
   - Keycloak tự động xử lý account linking
   - **Đây là cách CHUẨN NHẤT** nhưng cần thay đổi flow

## Kết luận

Impersonation là giải pháp:
- ✅ Bảo mật (không lưu password)
- ✅ Chuẩn Keycloak
- ✅ Cho phép user login linh hoạt
- ⚠️ Cần cấu hình đúng trong Keycloak
