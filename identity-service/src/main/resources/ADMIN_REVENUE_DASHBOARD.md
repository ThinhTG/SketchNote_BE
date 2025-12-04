# Admin Revenue Dashboard - Documentation

## Tổng quan

Module này cung cấp API thống kê doanh thu cho Admin Dashboard dựa trên logic **Admin Wallet**.

## Logic Nghiệp vụ

### Revenue = Admin Wallet Income

**Revenue được tính từ:**
1. **Subscription** - Tiền user mua gói đăng ký
2. **Token/AI Credits** - Tiền user mua credit AI

**Revenue KHÔNG bao gồm:**
- **Deposit** - User nạp tiền vào ví → Tiền này vẫn thuộc user, Admin chỉ giữ hộ
- **Withdraw** - User rút tiền từ ví → User lấy lại tiền của họ

### Tại sao logic này hợp lý?

1. **Tránh rủi ro thanh khoản**: Nếu tính Deposit vào revenue, Admin có thể "xài" tiền của user → không đủ để user withdraw
2. **Revenue = Thực thu từ dịch vụ**: Chỉ tính tiền từ bán Subscription + Token, đây mới là doanh thu thực sự
3. **Tách biệt rõ ràng**: 
   - Admin Wallet = Revenue (Subscription + Token)
   - User Wallet Balance = Nghĩa vụ phải trả (Liability)

## API Endpoints

Base URL: `/api/admin/revenue`

### 1. Tổng quan Admin Wallet
```
GET /api/admin/revenue/overview
```

Response:
```json
{
  "totalBalance": 10000000,       // Tổng revenue
  "subscriptionBalance": 8000000, // Từ Subscription
  "tokenBalance": 2000000,        // Từ Token/Credits
  "totalUserDeposits": 50000000,  // Tham khảo: User đã nạp
  "totalUserWithdrawals": 30000000, // Tham khảo: User đã rút
  "totalUserWalletBalance": 20000000 // Tổng tiền trong ví user (liability)
}
```

### 2. Thống kê doanh thu theo thời gian
```
GET /api/admin/revenue/stats?start=2024-01-01&end=2024-12-31&groupBy=month&type=all
```

Parameters:
- `start`: Ngày bắt đầu (format: yyyy-MM-dd), default 30 ngày trước
- `end`: Ngày kết thúc (format: yyyy-MM-dd), default hôm nay
- `groupBy`: "day" | "month" | "year", default "day"
- `type`: "all" | "subscription" | "token", default "all"

Response:
```json
{
  "totalRevenue": 10000000,
  "totalSubscriptionRevenue": 8000000,
  "totalTokenRevenue": 2000000,
  "subscriptionTransactionCount": 150,
  "tokenTransactionCount": 50,
  "subscriptionRevenueTimeSeries": [
    {"period": "2024-01", "amount": 2000000, "transactionCount": 40},
    {"period": "2024-02", "amount": 2500000, "transactionCount": 45}
  ],
  "tokenRevenueTimeSeries": [...],
  "totalRevenueTimeSeries": [...]
}
```

### 3. Dashboard tổng hợp
```
GET /api/admin/revenue/dashboard?start=2024-01-01&end=2024-12-31&groupBy=month
```

Response bao gồm: overview, stats, top subscriptions, top token packages

### 4. Top Subscriptions
```
GET /api/admin/revenue/top-subscriptions?limit=5
```

### 5. Top Token Packages
```
GET /api/admin/revenue/top-token-packages?limit=5
```

### 6. Tỷ lệ tăng trưởng
```
GET /api/admin/revenue/growth?start=2024-01-01&end=2024-01-31
```

So sánh doanh thu kỳ hiện tại với kỳ trước có cùng độ dài.

## Cấu trúc Code

```
identity-service/
├── controller/
│   └── AdminRevenueController.java      # REST API endpoints
├── service/
│   ├── interfaces/
│   │   └── IAdminRevenueService.java    # Service interface
│   └── implement/
│       └── AdminRevenueServiceImpl.java # Business logic
├── repository/
│   └── AdminRevenueRepository.java      # Database queries
└── dtos/response/admin/
    ├── AdminRevenueStatsDTO.java        # Revenue statistics
    ├── AdminWalletOverviewDTO.java      # Wallet overview
    └── AdminRevenueDashboardDTO.java    # Full dashboard
```

## Transaction Types được tính vào Revenue

```java
// Subscription revenue
SUBSCRIPTION
PURCHASE_SUBSCRIPTION

// Token/AI Credits revenue
PURCHASE_AI_CREDITS
```

## Transaction Types KHÔNG tính vào Revenue

```java
DEPOSIT       // User nạp tiền - không phải revenue
WITHDRAW      // User rút tiền - không phải revenue
PAYMENT       // Generic payment
COURSE_FEE    // Phí khóa học
PURCHASE_RESOURCE // Mua resource (có thể thêm nếu cần)
```

## Migration từ API cũ

### API cũ (order-service) - DEPRECATED
```
GET /api/orders/admin/dashboard/revenue
```

### API mới (identity-service)
```
GET /api/admin/revenue/stats
GET /api/admin/revenue/dashboard
```

## Best Practices

1. **Sử dụng groupBy hợp lý**:
   - `day`: Cho báo cáo tuần/tháng
   - `month`: Cho báo cáo năm
   - `year`: Cho báo cáo nhiều năm

2. **Cache response**: API có thể cache với TTL phù hợp vì dữ liệu revenue không thay đổi liên tục

3. **Phân quyền**: Tất cả endpoints yêu cầu role `ADMIN`

## Ví dụ sử dụng

### Dashboard hôm nay
```bash
curl -X GET "http://localhost:8080/api/admin/revenue/dashboard" \
  -H "Authorization: Bearer {token}"
```

### Revenue tháng này theo ngày
```bash
curl -X GET "http://localhost:8080/api/admin/revenue/stats?start=2024-12-01&end=2024-12-31&groupBy=day" \
  -H "Authorization: Bearer {token}"
```

### Revenue năm nay theo tháng
```bash
curl -X GET "http://localhost:8080/api/admin/revenue/stats?start=2024-01-01&end=2024-12-31&groupBy=month" \
  -H "Authorization: Bearer {token}"
```
