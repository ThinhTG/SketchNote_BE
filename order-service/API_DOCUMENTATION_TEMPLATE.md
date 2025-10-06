# API Documentation - /api/orders/template

## Tổng quan
Endpoint `/api/orders/template` cung cấp các chức năng quản lý template trong hệ thống order. Bao gồm CRUD operations và các tính năng tìm kiếm, lọc template.

## Base URL
```
/api/orders/template
```

## Endpoints

### 1. Lấy tất cả template đang active
```http
GET /api/orders/template
```

**Response:**
```json
[
  {
    "resourceTemplateId": 1,
    "designerId": 1,
    "name": "Business Presentation Template",
    "description": "Professional business presentation template",
    "type": "PRESENTATION",
    "price": 25.00,
    "expiredTime": "2025-01-15",
    "releaseDate": "2024-01-01",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00",
    "isActive": true
  }
]
```

### 2. Lấy template theo ID
```http
GET /api/orders/template/{id}
```

**Parameters:**
- `id` (path): ID của template

**Response:** ResourceTemplateDTO object

### 3. Lấy template theo designer
```http
GET /api/orders/template/designer/{designerId}
```

**Parameters:**
- `designerId` (path): ID của designer

### 4. Lấy template theo loại
```http
GET /api/orders/template/type/{type}
```

**Parameters:**
- `type` (path): Loại template (PRESENTATION, DOCUMENT, INFOGRAPHIC, POSTER, BROCHURE, CERTIFICATE, OTHER)

### 5. Tìm kiếm template
```http
GET /api/orders/template/search?keyword={keyword}
```

**Parameters:**
- `keyword` (query): Từ khóa tìm kiếm

### 6. Lấy template theo khoảng giá
```http
GET /api/orders/template/price-range?minPrice={minPrice}&maxPrice={maxPrice}
```

**Parameters:**
- `minPrice` (query): Giá tối thiểu
- `maxPrice` (query): Giá tối đa

### 7. Tạo template mới
```http
POST /api/orders/template
Content-Type: application/json

{
  "designerId": 1,
  "name": "New Template",
  "description": "Template description",
  "type": "PRESENTATION",
  "price": 30.00,
  "expiredTime": "2025-12-31",
  "releaseDate": "2024-01-15"
}
```

**Request Body:** TemplateCreateUpdateDTO

**Response:** ResourceTemplateDTO (201 Created)

### 8. Cập nhật template
```http
PUT /api/orders/template/{id}
Content-Type: application/json

{
  "name": "Updated Template Name",
  "description": "Updated description",
  "type": "DOCUMENT",
  "price": 35.00,
  "expiredTime": "2025-12-31"
}
```

**Parameters:**
- `id` (path): ID của template

**Request Body:** TemplateCreateUpdateDTO

**Response:** ResourceTemplateDTO

### 9. Xóa template (soft delete)
```http
DELETE /api/orders/template/{id}
```

**Parameters:**
- `id` (path): ID của template

**Response:** 204 No Content

### 10. Kích hoạt/vô hiệu hóa template
```http
PATCH /api/orders/template/{id}/toggle-status
```

**Parameters:**
- `id` (path): ID của template

**Response:** ResourceTemplateDTO

### 11. Lấy template theo trạng thái
```http
GET /api/orders/template/status/{isActive}
```

**Parameters:**
- `isActive` (path): true hoặc false

### 12. Lấy template sắp hết hạn
```http
GET /api/orders/template/expiring-soon?days={days}
```

**Parameters:**
- `days` (query): Số ngày (default: 7)

### 13. Lấy template mới nhất
```http
GET /api/orders/template/latest?limit={limit}
```

**Parameters:**
- `limit` (query): Số lượng template (default: 10)

### 14. Lấy template phổ biến nhất
```http
GET /api/orders/template/popular?limit={limit}
```

**Parameters:**
- `limit` (query): Số lượng template (default: 10)

## DTOs

### TemplateCreateUpdateDTO
```json
{
  "designerId": 1,
  "name": "Template Name",
  "description": "Template description",
  "type": "PRESENTATION",
  "price": 25.00,
  "expiredTime": "2025-01-15",
  "releaseDate": "2024-01-01"
}
```

### ResourceTemplateDTO
```json
{
  "resourceTemplateId": 1,
  "designerId": 1,
  "name": "Template Name",
  "description": "Template description",
  "type": "PRESENTATION",
  "price": 25.00,
  "expiredTime": "2025-01-15",
  "releaseDate": "2024-01-01",
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T00:00:00",
  "isActive": true
}
```

## Template Types
- `PRESENTATION`: Template thuyết trình
- `DOCUMENT`: Template tài liệu
- `INFOGRAPHIC`: Template infographic
- `POSTER`: Template poster
- `BROCHURE`: Template brochure
- `CERTIFICATE`: Template chứng chỉ
- `OTHER`: Loại khác

## Error Responses

### 404 Not Found
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Resource Template Not Found",
  "message": "Template not found with id: 999",
  "path": "/api/orders/template/999"
}
```

### 400 Bad Request
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid template type: INVALID_TYPE",
  "path": "/api/orders/template/type/INVALID_TYPE"
}
```

## Service Interface

### TemplateService
```java
public interface TemplateService {
    List<ResourceTemplateDTO> getAllActiveTemplates();
    ResourceTemplateDTO getTemplateById(Long id);
    List<ResourceTemplateDTO> getTemplatesByDesigner(Long designerId);
    List<ResourceTemplateDTO> getTemplatesByType(String type);
    List<ResourceTemplateDTO> searchTemplates(String keyword);
    List<ResourceTemplateDTO> getTemplatesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);
    ResourceTemplateDTO createTemplate(TemplateCreateUpdateDTO templateDTO);
    ResourceTemplateDTO updateTemplate(Long id, TemplateCreateUpdateDTO templateDTO);
    void deleteTemplate(Long id);
    ResourceTemplateDTO toggleTemplateStatus(Long id);
    List<ResourceTemplateDTO> getTemplatesByStatus(Boolean isActive);
    List<ResourceTemplateDTO> getTemplatesExpiringSoon(int days);
    List<ResourceTemplateDTO> getLatestTemplates(int limit);
    List<ResourceTemplateDTO> getPopularTemplates(int limit);
}
```

## Tính năng chính

1. **CRUD Operations**: Tạo, đọc, cập nhật, xóa template
2. **Soft Delete**: Xóa template bằng cách đặt isActive = false
3. **Search & Filter**: Tìm kiếm theo từ khóa, lọc theo loại, giá, designer
4. **Status Management**: Quản lý trạng thái active/inactive
5. **Expiry Management**: Theo dõi template sắp hết hạn
6. **Popularity Tracking**: Lấy template phổ biến nhất
7. **Latest Templates**: Lấy template mới nhất

## Repository Methods

ResourceTemplateRepository cung cấp các method:
- `findByIsActiveTrue()`: Lấy template active
- `findByDesignerIdAndIsActiveTrue(Long designerId)`: Lấy theo designer
- `findByTypeAndIsActiveTrue(TemplateType type)`: Lấy theo loại
- `findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice)`: Lấy theo giá
- `searchByKeyword(String keyword)`: Tìm kiếm theo từ khóa
- `findByIdAndIsActiveTrue(Long id)`: Lấy theo ID và active
- `findByIsActive(Boolean isActive)`: Lấy theo trạng thái
- `findByExpiredTimeBefore(LocalDate expiryDate)`: Lấy sắp hết hạn
- `countByDesignerIdAndIsActiveTrue(Long designerId)`: Đếm theo designer
- `existsByIdAndIsActiveTrue(Long id)`: Kiểm tra tồn tại và active
