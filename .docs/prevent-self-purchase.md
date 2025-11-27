# Ngăn Designer Mua Resource Của Chính Họ

## 📋 Tổng quan
Đã thực hiện các thay đổi để ngăn designer mua resource template của chính họ, bao gồm:
- Thêm field `isOwner` vào response để frontend có thể disable nút Buy
- Thêm validation server-side để chặn việc mua resource của chính mình

## ✅ Các thay đổi đã thực hiện

### 1. Thêm field `isOwner` vào ResourceTemplateDTO
**File:** `order-service/src/main/java/com/sketchnotes/order_service/dtos/ResourceTemplateDTO.java`

```java
// Indicates if the current user is the owner of this resource template
// Used by frontend to disable "Buy" button for own resources
private Boolean isOwner;
```

### 2. Cập nhật OrderTemplateController
**File:** `order-service/src/main/java/com/sketchnotes/order_service/controller/OrderTemplateController.java`

#### a. Thêm helper methods:
- `getCurrentUserId()`: Lấy ID của user hiện tại (trả về null nếu chưa đăng nhập)
- `setOwnerFlag(List<ResourceTemplateDTO>, Long)`: Set isOwner cho danh sách templates
- `setOwnerFlag(PagedResponseDTO<ResourceTemplateDTO>, Long)`: Set isOwner cho paged templates

#### b. Cập nhật các API để set `isOwner`:
- ✅ `GET /api/orders/template` - getAllActiveTemplates (marketplace chính)
- ✅ `GET /api/orders/template/type/{type}` - getTemplatesByTypePaged
- ✅ `GET /api/orders/template/search` - searchTemplates
- ✅ `GET /api/orders/template/popular` - getPopularTemplates
- ✅ `GET /api/orders/template/latest` - getLatestTemplates

### 3. Thêm validation server-side
**File:** `order-service/src/main/java/com/sketchnotes/order_service/service/implement/OrderServiceImpl.java`

**Method:** `validateOrderDuplicates()`

```java
// ✅ Validate: User cannot buy their own template
ResourceTemplate template = resourceTemplateRepository.findById(templateId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                String.format("Template %d not found", templateId)));

if (template.getDesignerId().equals(userId)) {
    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
            String.format("You cannot purchase your own template (ID: %d)", templateId));
}
```

## 🔒 Bảo mật

### Client-side (Frontend)
- Field `isOwner` trong response cho phép frontend:
  - Disable nút "Buy" cho resource của chính user
  - Hiển thị badge "Your Resource" hoặc tương tự
  - Cải thiện UX bằng cách ngăn user click vào resource của mình

### Server-side (Backend)
- Validation trong `OrderServiceImpl.createOrder()`:
  - Kiểm tra `designerId == userId` trước khi tạo order
  - Trả về HTTP 403 FORBIDDEN với message rõ ràng
  - **Không thể bypass** vì validation ở server

## 📊 Response Format

### Trước khi thay đổi:
```json
{
  "resourceTemplateId": 123,
  "designerId": 456,
  "name": "Beautiful Template",
  "price": 100000,
  "designerInfo": { ... }
}
```

### Sau khi thay đổi:
```json
{
  "resourceTemplateId": 123,
  "designerId": 456,
  "name": "Beautiful Template",
  "price": 100000,
  "designerInfo": { ... },
  "isOwner": true  // ← NEW FIELD
}
```

## 🧪 Test Cases

### Test 1: Marketplace API trả về isOwner
```bash
# Designer với ID = 100 gọi API marketplace
GET /api/orders/template
Authorization: Bearer <token_of_user_100>

# Response sẽ có:
# - isOwner: true cho templates có designerId = 100
# - isOwner: false cho templates của designer khác
```

### Test 2: Không thể mua resource của chính mình
```bash
# Designer với ID = 100 cố mua template của chính họ (templateId = 50, designerId = 100)
POST /api/orders
{
  "userId": 100,
  "items": [
    {
      "resourceTemplateId": 50
    }
  ]
}

# Response: 403 FORBIDDEN
{
  "error": "You cannot purchase your own template (ID: 50)"
}
```

### Test 3: User chưa đăng nhập
```bash
# Gọi API marketplace không có token
GET /api/orders/template

# Response: isOwner = null hoặc false cho tất cả templates
```

## 🎯 Frontend Implementation Suggestion

```javascript
// Example: Disable Buy button based on isOwner
function renderBuyButton(template) {
  if (template.isOwner) {
    return (
      <button disabled className="btn-disabled">
        Your Resource
      </button>
    );
  }
  
  return (
    <button onClick={() => buyTemplate(template.id)} className="btn-primary">
      Buy Now - {template.price} VND
    </button>
  );
}
```

## 📝 Notes

1. **Backward Compatible**: Field `isOwner` là optional (Boolean), nên không ảnh hưởng đến code cũ
2. **Performance**: Không ảnh hưởng performance vì chỉ thêm 1 comparison đơn giản
3. **Security**: Validation ở server là bắt buộc, không thể bypass từ client
4. **UX**: User sẽ thấy rõ resource nào là của họ trước khi click Buy

## 🚀 Deployment

Sau khi deploy, cần:
1. ✅ Restart order-service
2. ✅ Test các API marketplace
3. ✅ Test flow tạo order với resource của chính mình (phải bị chặn)
4. ✅ Cập nhật frontend để sử dụng field `isOwner`
