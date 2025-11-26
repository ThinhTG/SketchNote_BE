# Notification System - Quick Start Guide

## 📋 Overview

Hệ thống thông báo real-time cho SketchNote_BE với các tính năng:
- ✅ Thông báo real-time qua WebSocket
- ✅ REST API quản lý thông báo
- ✅ Tự động gửi thông báo khi mua hàng
- ✅ Hỗ trợ nhiều loại thông báo

## 🚀 API Endpoints

### User Endpoints (Yêu cầu JWT)

```bash
# Lấy danh sách thông báo (phân trang)
GET /api/notifications?page=0&size=20

# Đánh dấu đã đọc
PATCH /api/notifications/{id}/read

# Đánh dấu tất cả đã đọc
PATCH /api/notifications/read-all

# Đếm số thông báo chưa đọc
GET /api/notifications/count-unread
```

### Internal Endpoint (Cho microservices)

```bash
# Tạo thông báo (không cần auth)
POST /internal/notifications
```

## 🔌 WebSocket Connection

### JavaScript Example

```javascript
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

const socket = new SockJS('http://34.126.134.243:8089/ws-notifications');
const stompClient = Stomp.over(socket);

stompClient.connect({}, () => {
  const userId = getCurrentUserId();
  stompClient.subscribe(`/topic/notifications.${userId}`, (message) => {
    const notification = JSON.parse(message.body);
    console.log('Thông báo mới:', notification);
  });
});
```

## 📝 Notification Types

- `PURCHASE` - Thông báo cho designer khi tài nguyên được mua
- `PURCHASE_CONFIRM` - Xác nhận mua hàng cho buyer
- `SYSTEM` - Thông báo hệ thống
- `COMMENT` - Thông báo về bình luận
- `ENROLLMENT` - Thông báo đăng ký khóa học
- `SUBSCRIPTION` - Thông báo về gói đăng ký
- `WALLET` - Thông báo về ví

## 🧪 Testing

1. **Swagger UI**: http://localhost:8089/swagger-ui.html
2. **WebSocket Test**: Sử dụng code JavaScript ở trên
3. **End-to-End**: Tạo order → Thanh toán → Kiểm tra thông báo

## 📚 Documentation

- [Implementation Plan](file:///C:/Users/admin/.gemini/antigravity/brain/dd6737db-8062-4799-8d04-9ea57987d4fe/implementation_plan.md)
- [Walkthrough](file:///C:/Users/admin/.gemini/antigravity/brain/dd6737db-8062-4799-8d04-9ea57987d4fe/walkthrough.md)
- [Task List](file:///C:/Users/admin/.gemini/antigravity/brain/dd6737db-8062-4799-8d04-9ea57987d4fe/task.md)

## 🎯 Key Features

- **Real-time Push**: WebSocket với STOMP protocol
- **Pagination**: Hỗ trợ phân trang cho danh sách thông báo
- **Auto-notification**: Tự động gửi khi có sự kiện (mua hàng, etc.)
- **Clean Code**: Code sạch, dễ đọc, dễ debug với logging đầy đủ
- **Error Handling**: Xử lý lỗi toàn diện, không làm gián đoạn flow chính

## 🔧 Configuration

File: `identity-service/src/main/resources/application.yaml`

```yaml
websocket:
  allowed-origins: "http://localhost:3000,http://34.126.134.243:8888"
```

## 📦 Files Created

### Identity Service
- `NotificationType.java` - Enum các loại thông báo
- `Notification.java` - Entity
- `NotificationDto.java` - Response DTO
- `CreateNotificationRequest.java` - Request DTO
- `NotificationMapper.java` - Mapper
- `NotificationRepository.java` - Repository
- `INotificationService.java` - Service interface
- `NotificationService.java` - Service implementation
- `NotificationController.java` - REST controller
- `InternalNotificationController.java` - Internal API
- `WebSocketConfig.java` - WebSocket configuration

### Order Service
- `NotificationClient.java` - Feign client
- `CreateNotificationRequest.java` - DTO
- `NotificationDto.java` - DTO
- Updated `OrderPaymentServiceImpl.java` - Tích hợp gửi thông báo

## ✅ Status

**Implementation**: ✅ Complete  
**Testing**: ⏳ Pending  
**Documentation**: ✅ Complete

---

Chúc bạn code vui vẻ! 🎉
