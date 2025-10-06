# Order Service - Template Ordering System

## Tổng quan
Order Service cung cấp các chức năng để quản lý đơn hàng template và quản lý các template có sẵn trong hệ thống.

## Các Entity chính

### Order
- Quản lý thông tin đơn hàng
- Hỗ trợ cả đơn hàng template đơn lẻ và subscription
- Theo dõi trạng thái thanh toán và đơn hàng

### OrderDetail  
- Chi tiết các template trong đơn hàng
- Tính toán giá, discount và subtotal
- Liên kết với ResourceTemplate

### ResourceTemplate
- Quản lý thông tin template
- Hỗ trợ nhiều loại template (PRESENTATION, DOCUMENT, INFOGRAPHIC, etc.)
- Quản lý giá cả và trạng thái active

## API Endpoints

### Order Management

#### Tạo đơn hàng
```http
POST /api/orders
Content-Type: application/json

{
  "userId": 1,
  "subscriptionId": null,
  "items": [
    {
      "resourceTemplateId": 1,
      "discount": 0.00
    },
    {
      "resourceTemplateId": 2,
      "discount": 5.00
    }
  ]
}
```

#### Lấy thông tin đơn hàng
```http
GET /api/orders/{orderId}
```

#### Lấy danh sách đơn hàng của user
```http
GET /api/orders/user/{userId}
```

#### Cập nhật trạng thái đơn hàng
```http
PUT /api/orders/{orderId}/status?status=CONFIRMED
```

#### Cập nhật trạng thái thanh toán
```http
PUT /api/orders/{orderId}/payment-status?paymentStatus=PAID
```

### Template Management

#### Lấy tất cả template
```http
GET /api/templates
```

#### Lấy template theo ID
```http
GET /api/templates/{templateId}
```

#### Lấy template theo loại
```http
GET /api/templates/type/{type}
```
Các loại template: PRESENTATION, DOCUMENT, INFOGRAPHIC, POSTER, BROCHURE, CERTIFICATE, OTHER

#### Tìm kiếm template
```http
GET /api/templates/search?keyword=business
```

#### Lấy template theo khoảng giá
```http
GET /api/templates/price-range?minPrice=10.00&maxPrice=50.00
```

## Response Format

### OrderResponseDTO
```json
{
  "orderId": 1,
  "userId": 1,
  "resourceTemplateId": null,
  "subscriptionId": null,
  "totalAmount": 45.00,
  "paymentStatus": "PENDING",
  "orderStatus": "PENDING",
  "invoiceNumber": "INV-A1B2C3D4",
  "issueDate": "2024-01-15T10:30:00",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00",
  "items": [
    {
      "orderDetailId": 1,
      "orderId": 1,
      "resourceTemplateId": 1,
      "unitPrice": 25.00,
      "discount": 0.00,
      "subtotalAmount": 25.00,
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00",
      "templateName": "Business Presentation Template",
      "templateDescription": "Professional business presentation template",
      "templateType": "PRESENTATION"
    }
  ]
}
```

### ResourceTemplateDTO
```json
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
```

## Error Handling

Hệ thống có Global Exception Handler để xử lý các lỗi:

- `OrderNotFoundException`: Khi không tìm thấy đơn hàng
- `ResourceTemplateNotFoundException`: Khi không tìm thấy template
- `IllegalArgumentException`: Khi tham số không hợp lệ

Format lỗi:
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Order not found with id: 999",
  "path": "/api/orders/999"
}
```

## Tính năng chính

1. **Tạo đơn hàng**: Hỗ trợ đặt nhiều template trong một đơn hàng
2. **Tính toán tự động**: Tự động tính tổng tiền, subtotal với discount
3. **Quản lý template**: Tìm kiếm, lọc theo loại và giá
4. **Theo dõi trạng thái**: Quản lý trạng thái đơn hàng và thanh toán
5. **Validation**: Kiểm tra template tồn tại và active trước khi tạo đơn hàng
6. **Invoice**: Tự động tạo mã hóa đơn duy nhất

## Database Schema

Hệ thống sử dụng PostgreSQL với các bảng:
- `orders`: Lưu thông tin đơn hàng
- `order_details`: Chi tiết các template trong đơn hàng  
- `resource_template`: Thông tin các template có sẵn

Tất cả các bảng đều có timestamps tự động và các constraint phù hợp với ERD đã thiết kế.
