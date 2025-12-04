# Withdrawal Wallet Feature

## Overview
This feature allows customers to request withdrawal of their wallet balance to their personal bank account. The withdrawal flow involves both Customer and Staff, including notifications and status updates.

## Features Implemented

### 1. Entity & Database
- **WithdrawalRequest** entity with complete fields
- **WithdrawalStatus** enum (PENDING, APPROVED, REJECTED)
- Database migration script (V7__add_withdrawal_system.sql)
- Proper indexes for performance optimization

### 2. Data Transfer Objects (DTOs)
- `WithdrawalRequestDto` - For creating withdrawal requests
- `WithdrawalResponse` - Response with complete withdrawal details
- `RejectWithdrawalRequest` - For rejecting with optional reason

### 3. Repository Layer
- `WithdrawalRequestRepository` with custom queries:
  - Find by user ID
  - Find by status
  - Check for pending requests
  - Find by ID and user ID (for authorization)

### 4. Service Layer
- `IWithdrawalService` interface
- `WithdrawalService` implementation with:
  - Transaction management
  - Wallet balance validation
  - Automatic notifications
  - Money locking/unlocking mechanism

### 5. Controller Layer
- **WithdrawalController** (Customer endpoints)
- **AdminWithdrawalController** (Staff endpoints)

### 6. Error Handling
Added custom error codes:
- `WITHDRAWAL_NOT_FOUND` (404)
- `PENDING_WITHDRAWAL_EXISTS` (409)
- `INVALID_WITHDRAWAL_AMOUNT` (400)
- `WITHDRAWAL_ALREADY_PROCESSED` (409)
- `WITHDRAWAL_FAILED` (500)

## API Endpoints

### Customer Endpoints

#### 1. Request Withdrawal
```
POST /api/withdraw/request
```

**Request Body:**
```json
{
  "amount": 500000,
  "bankName": "Vietcombank",
  "bankAccountNumber": "0123456789",
  "bankAccountHolder": "Nguyen Van A"
}
```

**Validation Rules:**
- Amount > 0
- Amount ≤ wallet balance
- Bank information required
- No pending withdrawal request

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Withdrawal request submitted successfully. Your money will be sent to your bank account within 24 hours.",
  "result": {
    "id": 1,
    "userId": 123,
    "amount": 500000,
    "bankName": "Vietcombank",
    "bankAccountNumber": "0123456789",
    "bankAccountHolder": "Nguyen Van A",
    "status": "PENDING",
    "staffId": null,
    "rejectionReason": null,
    "createdAt": "2025-05-01T10:20:00",
    "updatedAt": "2025-05-01T10:20:00"
  }
}
```

**Error Responses:**
- 400: "Insufficient wallet balance"
- 400: "Invalid withdrawal amount"
- 409: "You already have a pending withdrawal request"
- 404: "Wallet not found. Please create a wallet first"

#### 2. Get Withdrawal History
```
GET /api/withdraw/my-history
```

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Withdrawal history retrieved successfully",
  "result": [
    {
      "id": 1,
      "userId": 123,
      "amount": 500000,
      "status": "APPROVED",
      "createdAt": "2025-05-01T10:20:00",
      "updatedAt": "2025-05-01T15:30:00"
    }
  ]
}
```

### Staff/Admin Endpoints

#### 3. Approve Withdrawal
```
PUT /api/admin/withdraw/{id}/approve
```

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Withdrawal approved and marked as completed.",
  "result": {
    "id": 1,
    "userId": 123,
    "amount": 500000,
    "status": "APPROVED",
    "staffId": 456,
    "createdAt": "2025-05-01T10:20:00",
    "updatedAt": "2025-05-01T15:30:00"
  }
}
```

**Error Responses:**
- 404: "Withdrawal request not found"
- 409: "Request is already processed"

#### 4. Reject Withdrawal
```
PUT /api/admin/withdraw/{id}/reject
```

**Request Body (Optional):**
```json
{
  "rejectionReason": "Invalid bank account information"
}
```

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Withdrawal request has been rejected.",
  "result": {
    "id": 1,
    "userId": 123,
    "amount": 500000,
    "status": "REJECTED",
    "staffId": 456,
    "rejectionReason": "Invalid bank account information",
    "createdAt": "2025-05-01T10:20:00",
    "updatedAt": "2025-05-01T15:30:00"
  }
}
```

#### 5. Get Pending Withdrawals
```
GET /api/admin/withdraw/pending
```

**Success Response (200):**
```json
{
  "code": 200,
  "message": "Pending withdrawal requests retrieved successfully",
  "result": [...]
}
```

#### 6. Get All Withdrawals
```
GET /api/admin/withdraw/all
```

**Success Response (200):**
```json
{
  "code": 200,
  "message": "All withdrawal requests retrieved successfully",
  "result": [...]
}
```

## Notifications

### Customer Notifications

1. **After Submission:**
   - Title: "Withdrawal Request Submitted"
   - Message: "Your withdrawal request was successfully submitted. Your money will be returned to your bank account within 24 hours."

2. **After Approval:**
   - Title: "Withdrawal Completed"
   - Message: "Your withdrawal has been completed. Please check your bank account."

3. **After Rejection:**
   - Title: "Withdrawal Request Rejected"
   - Message: "Your withdrawal request has been rejected. Please contact support for more information."

## Workflow

### Step 1: Customer Requests Withdrawal
1. Validate user authentication
2. Check wallet balance
3. Check for pending requests
4. Deduct amount from wallet (lock the money)
5. Create withdrawal request record
6. Send notification to customer

### Step 2: Staff Processes Request
1. Review pending requests via `/api/admin/withdraw/pending`
2. Manually transfer money to customer's bank account
3. Either:
   - **Approve**: Update status to APPROVED, send success notification
   - **Reject**: Refund money to wallet, update status to REJECTED, send rejection notification

### Step 3: Customer Receives Notification
Customer sees appropriate notification based on staff action.

## Business Rules

1. **Money Locking**: When a withdrawal request is created, the amount is immediately deducted from the wallet balance to prevent double-spending.

2. **Refund on Rejection**: If staff rejects the request, the money is automatically refunded to the wallet.

3. **Single Pending Request**: Users can only have one pending withdrawal request at a time.

4. **Immutable Processed Requests**: Once a request is approved or rejected, it cannot be changed again.

5. **Staff Tracking**: Every approved/rejected request records which staff member processed it.

## Security Considerations

1. **Authentication Required**: All endpoints require user authentication.
2. **Authorization**: Customers can only view their own withdrawal history.
3. **Admin-Only Access**: Approval/rejection endpoints are restricted to admin/staff roles.
4. **Input Validation**: All inputs are validated using Bean Validation annotations.

## Error Handling

All errors follow the standard ApiResponse format:
```json
{
  "code": 400,
  "message": "Error message in English",
  "result": null
}
```

HTTP Status Codes:
- 200: Success
- 400: Bad Request (validation errors, insufficient balance)
- 404: Not Found (withdrawal/wallet/user not found)
- 409: Conflict (duplicate pending request, already processed)
- 500: Internal Server Error

## Database Schema

```sql
CREATE TABLE withdrawal_request (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    bank_name VARCHAR(100) NOT NULL,
    bank_account_number VARCHAR(50) NOT NULL,
    bank_account_holder VARCHAR(200) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    staff_id BIGINT,
    rejection_reason TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

## Testing Checklist

- [ ] Create withdrawal request with valid data
- [ ] Create withdrawal request with insufficient balance
- [ ] Create withdrawal request with invalid amount (negative or zero)
- [ ] Create second pending request (should fail)
- [ ] Approve withdrawal request
- [ ] Reject withdrawal request (verify money refunded)
- [ ] Try to approve already processed request (should fail)
- [ ] Get withdrawal history
- [ ] Get pending withdrawals (staff)
- [ ] Get all withdrawals (staff)
- [ ] Verify notifications are sent correctly

## Files Created

### Models & Enums
- `WithdrawalRequest.java` - Entity
- `WithdrawalStatus.java` - Enum

### DTOs
- `WithdrawalRequestDto.java` - Request DTO
- `RejectWithdrawalRequest.java` - Rejection request DTO
- `WithdrawalResponse.java` - Response DTO

### Repository
- `WithdrawalRequestRepository.java` - JPA Repository

### Service
- `IWithdrawalService.java` - Service interface
- `WithdrawalService.java` - Service implementation

### Controllers
- `WithdrawalController.java` - Customer endpoints
- `AdminWithdrawalController.java` - Staff/admin endpoints

### Database
- `V7__add_withdrawal_system.sql` - Flyway migration

### Configuration
- Updated `ErrorCode.java` with withdrawal-specific errors

## Clean Code Practices Applied

1. ✅ **Separation of Concerns**: Clear separation between controller, service, and repository layers
2. ✅ **Single Responsibility**: Each class has one clear purpose
3. ✅ **Dependency Injection**: Using constructor injection with Lombok's @RequiredArgsConstructor
4. ✅ **Validation**: Using Bean Validation annotations
5. ✅ **Transaction Management**: Using @Transactional where needed
6. ✅ **Error Handling**: Consistent error handling with custom exceptions
7. ✅ **Documentation**: Comprehensive JavaDoc comments
8. ✅ **Logging**: Proper logging at INFO level for important operations
9. ✅ **Naming Conventions**: Clear, descriptive names for variables and methods
10. ✅ **Immutability**: Using @Builder and final fields where appropriate

## Future Enhancements

1. Add pagination for withdrawal history
2. Add filters (by status, date range) for admin withdrawal list
3. Add withdrawal limits (daily/monthly)
4. Add webhook for automatic bank transfer integration
5. Add email notifications in addition to in-app notifications
6. Add withdrawal fee calculation
7. Add batch approval for multiple requests
8. Add export functionality (CSV/Excel) for admin
