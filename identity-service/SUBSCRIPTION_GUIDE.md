# Subscription Plan System - Implementation Guide

## Overview

Hệ thống Subscription Plan cho phép người dùng nâng cấp tài khoản từ Free tier lên Customer Pro hoặc Designer để mở khóa các tính năng premium.

## Business Logic

### Free Tier (Mặc định)
- **Quota**: Tối đa 3 projects
- **Tính năng**: View và edit projects hiện có
- **Giới hạn**: Không thể tạo project mới khi đã đạt 3 projects

### Customer Pro Subscription
- **Quota**: Unlimited projects
- **Thời hạn**: 30 ngày (monthly) hoặc 365 ngày (yearly)
- **Giá**: 99,000 VND/tháng hoặc 990,000 VND/năm
- **Khi hết hạn**: Không thể tạo project mới, vẫn view/edit được tất cả projects cũ

### Designer Subscription
- **Quota**: Unlimited projects
- **Role**: Được nâng lên DESIGNER role (permanent)
- **Tính năng**: Có thể đăng bán sản phẩm trên platform
- **Thời hạn**: 30 ngày (monthly) hoặc 365 ngày (yearly)
- **Giá**: 199,000 VND/tháng hoặc 1,990,000 VND/năm
- **Khi hết hạn**: 
  - Giữ DESIGNER role
  - Vô hiệu hóa tính năng bán hàng
  - Không thể tạo project mới
  - Vẫn view/edit được tất cả projects cũ

## Database Setup

### 1. Start the application
Application sẽ tự động tạo tables thông qua JPA:
- `subscription_plan`
- `user_subscription`

### 2. Seed default subscription plans
Chạy script SQL để tạo các gói subscription mặc định:

```bash
# Connect to your database and run:
psql -U your_username -d your_database -f src/main/resources/seed_subscription_plans.sql
```

Hoặc copy nội dung file `seed_subscription_plans.sql` và execute trong database client.

## API Endpoints

### Subscription Plans (Public & Admin)

#### Get all active plans (Public)
```http
GET /api/subscription-plans
```

#### Get plan by ID (Public)
```http
GET /api/subscription-plans/{id}
```

#### Create plan (Admin only)
```http
POST /api/subscription-plans
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "planName": "Customer Pro - Monthly",
  "planType": "CUSTOMER_PRO",
  "price": 99000,
  "currency": "VND",
  "durationDays": 30,
  "description": "Unlimited projects for 30 days",
  "isActive": true
}
```

#### Update plan (Admin only)
```http
PUT /api/subscription-plans/{id}
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "planName": "Customer Pro - Monthly (Updated)",
  "planType": "CUSTOMER_PRO",
  "price": 89000,
  "currency": "VND",
  "durationDays": 30,
  "description": "Updated description",
  "isActive": true
}
```

#### Deactivate plan (Admin only)
```http
DELETE /api/subscription-plans/{id}
Authorization: Bearer {admin_token}
```

### User Subscriptions

#### Purchase subscription
```http
POST /api/users/me/subscriptions
Authorization: Bearer {user_token}
Content-Type: application/json

{
  "planId": 1,
  "autoRenew": false
}
```

**Response:**
```json
{
  "code": 200,
  "result": {
    "subscriptionId": 1,
    "userId": 123,
    "plan": {
      "planId": 1,
      "planName": "Customer Pro - Monthly",
      "planType": "CUSTOMER_PRO",
      "price": 99000,
      "currency": "VND",
      "durationDays": 30,
      "description": "Unlimited projects for 30 days"
    },
    "status": "ACTIVE",
    "startDate": "2025-11-21T15:00:00",
    "endDate": "2025-12-21T15:00:00",
    "autoRenew": false,
    "isCurrentlyActive": true
  },
  "message": "Subscription purchased successfully"
}
```

#### Get my subscriptions
```http
GET /api/users/me/subscriptions
Authorization: Bearer {user_token}
```

#### Get active subscription
```http
GET /api/users/me/subscriptions/active
Authorization: Bearer {user_token}
```

#### Get quota status
```http
GET /api/users/me/subscriptions/quota
Authorization: Bearer {user_token}
```

**Response:**
```json
{
  "code": 200,
  "result": {
    "maxProjects": -1,
    "currentProjects": 5,
    "remainingProjects": null,
    "subscriptionType": "Customer Pro - Monthly",
    "hasActiveSubscription": true,
    "canCreateProject": true
  },
  "message": "Retrieved user quota"
}
```

#### Cancel subscription
```http
DELETE /api/users/me/subscriptions/{subscriptionId}
Authorization: Bearer {user_token}
```

## Project Quota Enforcement

### Project Creation Flow

1. User gọi `POST /api/projects` để tạo project mới
2. Project-service gọi Identity-service để check quota
3. Nếu `canCreateProject = false` → throw `PROJECT_QUOTA_EXCEEDED` error
4. Nếu `canCreateProject = true` → tạo project thành công

### Error Response khi vượt quota
```json
{
  "code": 400,
  "message": "Project quota exceeded. Please upgrade your subscription"
}
```

## Scheduled Jobs

### Subscription Expiration Job
- **Schedule**: Chạy hàng ngày lúc 00:00 (midnight)
- **Function**: Tự động đánh dấu các subscription đã hết hạn thành `EXPIRED`
- **Implementation**: `UserSubscriptionService.processExpiredSubscriptions()`

## Testing Guide

### 1. Test Free Tier Quota
```bash
# Create 3 projects as free user
POST /api/projects (success)
POST /api/projects (success)
POST /api/projects (success)
POST /api/projects (fail - quota exceeded)
```

### 2. Test Subscription Purchase
```bash
# Check current quota
GET /api/users/me/subscriptions/quota
# Response: maxProjects: 3, canCreateProject: false

# Purchase Customer Pro
POST /api/users/me/subscriptions
{
  "planId": 1,
  "autoRenew": false
}

# Check quota again
GET /api/users/me/subscriptions/quota
# Response: maxProjects: -1 (unlimited), canCreateProject: true

# Create 4th project
POST /api/projects (success!)
```

### 3. Test Designer Upgrade
```bash
# Check current role
GET /api/users/me
# Response: role: "CUSTOMER"

# Purchase Designer subscription
POST /api/users/me/subscriptions
{
  "planId": 3,
  "autoRenew": false
}

# Check role again
GET /api/users/me
# Response: role: "DESIGNER"
```

### 4. Test Subscription Expiration
```bash
# Manually update endDate in database to past date
UPDATE user_subscription SET end_date = NOW() - INTERVAL '1 day' WHERE subscription_id = 1;

# Trigger scheduled job manually or wait for midnight
# Or call the service method directly in a test

# Check subscription status
GET /api/users/me/subscriptions/active
# Response: null (no active subscription)

# Check quota
GET /api/users/me/subscriptions/quota
# Response: maxProjects: 3, canCreateProject: depends on current project count

# Try to create project
POST /api/projects
# If user has > 3 projects: fail with quota exceeded
# If user has < 3 projects: success
```

## Admin Configuration

Admin có thể tạo các gói subscription tùy chỉnh:

```bash
POST /api/subscription-plans
{
  "planName": "Enterprise - Quarterly",
  "planType": "DESIGNER",
  "price": 499000,
  "currency": "VND",
  "durationDays": 90,
  "description": "Designer plan for 3 months",
  "isActive": true
}
```

## Notes

- Wallet balance sẽ bị trừ khi mua subscription
- Designer role là permanent (không bị downgrade khi subscription hết hạn)
- Tính năng bán hàng sẽ bị vô hiệu hóa khi Designer subscription hết hạn (implement ở order-service)
- Project quota được enforce ở project-service thông qua Feign client
- Nếu identity-service down, project creation vẫn được phép (fail-open strategy)
