# Pagination & PayOS Integration Documentation

## Pagination Features

### PagedResponseDTO Structure
```json
{
  "content": [...],
  "page": 0,
  "size": 10,
  "totalElements": 100,
  "totalPages": 10,
  "first": true,
  "last": false,
  "hasNext": true,
  "hasPrevious": false
}
```

### Pagination Endpoints

#### 1. Get All Templates with Pagination
```http
GET /api/orders/template/paged?page=0&size=10&sortBy=createdAt&sortDir=desc
```

**Parameters:**
- `page` (default: 0): Số trang (0-based)
- `size` (default: 10): Số item per page
- `sortBy` (default: createdAt): Field để sort
- `sortDir` (default: desc): asc hoặc desc

#### 2. Get Templates by Designer with Pagination
```http
GET /api/orders/template/designer/{designerId}/paged?page=0&size=10&sortBy=price&sortDir=asc
```

#### 3. Get Templates by Type with Pagination
```http
GET /api/orders/template/type/{type}/paged?page=0&size=10&sortBy=name&sortDir=asc
```

#### 4. Search Templates with Pagination
```http
GET /api/orders/template/search/paged?keyword=business&page=0&size=10&sortBy=createdAt&sortDir=desc
```

#### 5. Get Templates by Price Range with Pagination
```http
GET /api/orders/template/price-range/paged?minPrice=10&maxPrice=50&page=0&size=10&sortBy=price&sortDir=asc
```

### Available Sort Fields
- `createdAt`: Ngày tạo
- `updatedAt`: Ngày cập nhật
- `name`: Tên template
- `price`: Giá
- `releaseDate`: Ngày phát hành

## PayOS Payment Integration

### Configuration

#### Environment Variables
```bash
PAYOS_CLIENT_ID=your-client-id
PAYOS_API_KEY=your-api-key
PAYOS_CHECKSUM_KEY=your-checksum-key
APP_BASE_URL=http://localhost:8080
```

#### application.yml
```yaml
payos:
  api-url: https://api-merchant.payos.vn/v2
  client-id: ${PAYOS_CLIENT_ID}
  api-key: ${PAYOS_API_KEY}
  checksum-key: ${PAYOS_CHECKSUM_KEY}

app:
  base-url: ${APP_BASE_URL}
```

### Payment Endpoints

#### 1. Create Payment Link for Order
```http
POST /api/orders/{orderId}/payment
```

**Response:**
```json
{
  "paymentId": "payment-123",
  "orderCode": "1234567890",
  "amount": 25.00,
  "description": "Payment for Order #INV-A1B2C3D4",
  "paymentUrl": "https://pay.payos.vn/web/...",
  "status": "PENDING",
  "createdAt": "2024-01-15T10:30:00",
  "expiredAt": "2024-01-15T10:45:00"
}
```

#### 2. Check Payment Status
```http
GET /api/payments/status/{orderCode}
```

#### 3. Cancel Payment Link
```http
POST /api/payments/cancel/{orderCode}
```

#### 4. PayOS Webhook Endpoint
```http
POST /api/payments/webhook
```

**Webhook Payload:**
```json
{
  "code": "00",
  "desc": "success",
  "data": "{\"orderCode\":\"1234567890\",\"amount\":2500,\"description\":\"Payment for Order #INV-A1B2C3D4\",\"accountNumber\":\"970422\",\"reference\":\"1234567890\",\"transactionDateTime\":\"2024-01-15T10:30:00Z\",\"currency\":\"VND\",\"paymentLinkId\":\"payment-123\",\"code\":\"00\",\"desc\":\"success\",\"counterAccountBankId\":\"970422\",\"counterAccountBankName\":\"MB\",\"counterAccountName\":\"NGUYEN VAN A\",\"counterAccountNumber\":\"1234567890\",\"virtualAccountName\":\"NGUYEN VAN A\",\"virtualAccountNumber\":\"1234567890\"}",
  "signature": "signature-string",
  "checksum": "checksum-string"
}
```

### Payment Flow

1. **Create Order**: User tạo order với template
2. **Create Payment**: Gọi `/api/orders/{orderId}/payment` để tạo payment link
3. **Redirect to PayOS**: Redirect user đến `paymentUrl` từ response
4. **Payment Processing**: User thanh toán trên PayOS
5. **Webhook Notification**: PayOS gửi webhook về `/api/payments/webhook`
6. **Order Update**: Hệ thống tự động cập nhật trạng thái order

### Payment Service Interface

```java
public interface PaymentService {
    PaymentResponseDTO createPaymentLink(PaymentRequestDTO paymentRequest);
    PaymentResponseDTO getPaymentStatus(String orderCode);
    boolean cancelPaymentLink(String orderCode);
    boolean handleWebhook(PayOSWebhookDTO webhookData);
    boolean verifyWebhookSignature(String signature, String data);
}
```

### DTOs

#### PaymentRequestDTO
```json
{
  "orderId": 1,
  "amount": 25.00,
  "description": "Payment for Order #INV-A1B2C3D4",
  "returnUrl": "http://localhost:3000/payment/success",
  "cancelUrl": "http://localhost:3000/payment/cancel",
  "items": [
    {
      "name": "Business Presentation Template",
      "quantity": 1,
      "price": 25.00
    }
  ]
}
```

#### PaymentResponseDTO
```json
{
  "paymentId": "payment-123",
  "orderCode": "1234567890",
  "amount": 25.00,
  "description": "Payment for Order #INV-A1B2C3D4",
  "paymentUrl": "https://pay.payos.vn/web/...",
  "status": "PENDING",
  "createdAt": "2024-01-15T10:30:00",
  "expiredAt": "2024-01-15T10:45:00"
}
```

### Error Handling

#### Payment Creation Failed
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Failed to create payment link: Invalid API key",
  "path": "/api/orders/1/payment"
}
```

#### Webhook Verification Failed
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid webhook signature",
  "path": "/api/payments/webhook"
}
```

### Security Considerations

1. **API Key Security**: Lưu trữ PayOS API keys trong environment variables
2. **Webhook Verification**: Luôn verify signature của webhook
3. **HTTPS**: Sử dụng HTTPS cho tất cả API calls
4. **Checksum Validation**: Validate checksum cho mọi request đến PayOS

### Testing

#### Test Payment Creation
```bash
curl -X POST http://localhost:8080/api/orders/1/payment \
  -H "Content-Type: application/json"
```

#### Test Webhook (for development)
```bash
curl -X POST http://localhost:8080/api/payments/webhook \
  -H "Content-Type: application/json" \
  -d '{
    "code": "00",
    "desc": "success",
    "data": "{\"orderCode\":\"1234567890\",\"amount\":2500,\"description\":\"Test payment\"}",
    "signature": "test-signature",
    "checksum": "test-checksum"
  }'
```

### Integration Steps

1. **Setup PayOS Account**: Đăng ký tài khoản PayOS và lấy API credentials
2. **Configure Environment**: Set các environment variables
3. **Deploy Application**: Deploy với HTTPS enabled
4. **Configure Webhook URL**: Set webhook URL trong PayOS dashboard
5. **Test Integration**: Test payment flow end-to-end

### Monitoring

- Log tất cả payment requests và responses
- Monitor webhook delivery success rate
- Track payment success/failure rates
- Alert on payment processing errors
