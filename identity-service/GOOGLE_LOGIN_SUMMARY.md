# Google OAuth Login - Summary

## Vấn đề ban đầu
❌ Khi user đã đăng nhập rồi mà login bằng Google, hệ thống **THAY ĐỔI PASSWORD** của user
→ User không thể login bằng email/password nữa!

## Giải pháp đã implement
✅ Sử dụng **Keycloak Impersonation** để login existing users mà **KHÔNG** thay đổi password

## Cách hoạt động

### 1. User MỚI (chưa có account)
```
User login Google lần đầu
    ↓
Tạo user trong Keycloak với random password
    ↓
Tạo user trong database
    ↓
Login bằng password grant
    ↓
✅ Thành công
```

### 2. User CŨ (đã có account)
```
User login Google lần 2
    ↓
Tìm thấy user trong database (theo email)
    ↓
KHÔNG thay đổi password ✅
    ↓
Lấy admin token
    ↓
Impersonate user (admin đóng giả user)
    ↓
Lấy token cho user
    ↓
✅ Thành công (password vẫn giữ nguyên!)
```

## Code Changes

### 1. AuthenticationService.java
- ❌ **XÓA**: Logic reset password cho existing users
- ✅ **THÊM**: Logic impersonation cho existing users
- ✅ **GIỮ NGUYÊN**: Logic tạo user mới

### 2. IdentityClient.java
- ✅ **THÊM**: Method `impersonateUser()` để gọi Keycloak API

### 3. ErrorCode.java
- ✅ **THÊM**: `GOOGLE_LOGIN_NOT_SUPPORTED_FOR_EXISTING_USERS` (fallback nếu impersonation fail)

## Cần làm gì tiếp theo?

### ⚠️ QUAN TRỌNG: Cấu hình Keycloak

Bạn PHẢI enable impersonation trong Keycloak:

1. Vào Keycloak Admin Console
2. Chọn Client → Service Account Roles
3. Thêm role: **`impersonation`** từ `realm-management`

📖 **Chi tiết**: Xem file `KEYCLOAK_IMPERSONATION_SETUP.md`

## Test Cases

### Test 1: User mới login Google
```
Email: newuser@gmail.com (chưa tồn tại)
Expected: Tạo user mới, login thành công
```

### Test 2: User cũ login Google (đã register bằng email/password)
```
Email: olduser@gmail.com (đã tồn tại, có password riêng)
Expected: Login thành công, password KHÔNG đổi
```

### Test 3: User cũ vẫn login được bằng email/password
```
Email: olduser@gmail.com
Password: 123456 (password cũ)
Expected: Login thành công
```

## Ưu điểm của giải pháp này

✅ **Bảo mật**: Không lưu password vào database
✅ **Chuẩn Keycloak**: Sử dụng tính năng có sẵn
✅ **Linh hoạt**: User có thể login bằng cả email/password VÀ Google
✅ **Không mất dữ liệu**: Password cũ được giữ nguyên

## Lưu ý

⚠️ **Impersonation phải được enable trong Keycloak**
⚠️ **Chỉ service account mới có quyền impersonate**
⚠️ **Client secret phải được bảo mật**

## Nếu không muốn dùng Impersonation?

Có thể dùng giải pháp đơn giản hơn:
- Chỉ cho phép Google login với user MỚI
- User cũ phải login bằng email/password

Nhưng giải pháp này **UX không tốt** vì user không thể login Google nếu đã có account.
