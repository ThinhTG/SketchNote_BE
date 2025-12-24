# Transaction API Refactoring Summary

## 🎯 Objective
Refactor wallet transaction endpoints to return DTO instead of exposing Entity directly.

## ✅ Changes Made

### 1. Created `TransactionResponse` DTO
**File:** `dtos/response/TransactionResponse.java`

```java
@Data
@Builder
public class TransactionResponse {
    private Long transactionId;
    private Long orderId;
    private BigDecimal amount;
    private BigDecimal balance;      // Balance after transaction
    private TransactionType type;
    private PaymentStatus status;
    private String provider;
    private String description;
    private LocalDateTime createdAt;
}
```

**Fields included:**
- ✅ `transactionId` - Transaction ID
- ✅ `orderId` - Related order ID
- ✅ `amount` - Transaction amount
- ✅ `balance` - Balance after transaction
- ✅ `type` - DEPOSIT, PAYMENT, WITHDRAW
- ✅ `status` - PENDING, SUCCESS, FAILED
- ✅ `provider` - MOMO, PAYOS
- ✅ `description` - Transaction description
- ✅ `createdAt` - Transaction timestamp

**Fields excluded (not exposed to API):**
- ❌ `externalTransactionId` - Internal only
- ❌ `orderCode` - Internal only
- ❌ `wallet` - Sensitive, not needed

### 2. Created `TransactionMapper`
**File:** `mapper/TransactionMapper.java`

```java
public class TransactionMapper {
    public static TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .transactionId(transaction.getTransactionId())
                .orderId(transaction.getOrderId())
                .amount(transaction.getAmount())
                .balance(transaction.getBalance())
                .type(transaction.getType())
                .status(transaction.getStatus())
                .provider(transaction.getProvider())
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
```

### 3. Updated `WalletController` Endpoints

#### Endpoint 1: `/api/wallet/charge-course`
**Before:**
```java
@PostMapping("/charge-course")
public ApiResponse<Transaction> chargeCourse(@RequestParam BigDecimal price)
```

**After:**
```java
@PostMapping("/charge-course")
public ApiResponse<TransactionResponse> chargeCourse(@RequestParam BigDecimal price) {
    // ... logic ...
    Transaction transaction = walletService.chargeCourse(wallet.getWalletId(), price);
    TransactionResponse response = TransactionMapper.toResponse(transaction);
    return ApiResponse.success(response, "Course charged successfully");
}
```

#### Endpoint 2: `/api/wallet/internal/pay-order`
**Before:**
```java
@PostMapping("/internal/pay-order")
public ApiResponse<Transaction> payOrder(...)
```

**After:**
```java
@PostMapping("/internal/pay-order")
public ApiResponse<TransactionResponse> payOrder(
        @RequestParam Long userId,
        @RequestParam BigDecimal amount,
        @RequestParam(required = false) String description) {
    // ... logic ...
    Transaction transaction = walletService.pay(wallet.getWalletId(), amount);
    TransactionResponse response = TransactionMapper.toResponse(transaction);
    return ApiResponse.success(response, "Order paid successfully");
}
```

#### Endpoint 3: `/api/wallet/internal/deposit-for-designer`
**Before:**
```java
@PostMapping("/internal/deposit-for-designer")
public ApiResponse<Transaction> depositForDesigner(...)
```

**After:**
```java
@PostMapping("/internal/deposit-for-designer")
public ApiResponse<TransactionResponse> depositForDesigner(
        @RequestParam Long designerId,
        @RequestParam BigDecimal amount,
        @RequestParam(required = false) String description) {
    // ... logic ...
    Transaction transaction = walletService.deposit(wallet.getWalletId(), amount);
    TransactionResponse response = TransactionMapper.toResponse(transaction);
    return ApiResponse.success(response, "Deposit for designer successful");
}
```

## 📊 API Response Format

### Before (Entity):
```json
{
  "code": 200,
  "message": "Course charged successfully",
  "result": {
    "transactionId": 123,
    "orderId": 456,
    "amount": 100000,
    "balance": 500000,
    "type": "PAYMENT",
    "status": "SUCCESS",
    "provider": "WALLET",
    "externalTransactionId": "ext-123",  // ← Exposed (not good)
    "description": "Course purchase",
    "createdAt": "2025-12-24T16:00:00",
    "orderCode": 789,                     // ← Exposed (not good)
    "wallet": {                           // ← Exposed (not good)
      "walletId": 1,
      "userId": 10,
      "balance": 500000
    }
  }
}
```

### After (DTO):
```json
{
  "code": 200,
  "message": "Course charged successfully",
  "result": {
    "transactionId": 123,
    "orderId": 456,
    "amount": 100000,
    "balance": 500000,
    "type": "PAYMENT",
    "status": "SUCCESS",
    "provider": "WALLET",
    "description": "Course purchase",
    "createdAt": "2025-12-24T16:00:00"
  }
}
```

## 💡 Benefits

### 1. **Security**
- ✅ No longer expose sensitive fields (`wallet`, `externalTransactionId`, `orderCode`)
- ✅ Control exactly what data is sent to clients

### 2. **Flexibility**
- ✅ Can change Entity structure without breaking API
- ✅ Different DTOs for different use cases

### 3. **Performance**
- ✅ Smaller payload (no unnecessary fields)
- ✅ No lazy-loading issues with `@JsonIgnore`

### 4. **Maintainability**
- ✅ Clear separation between persistence layer and API layer
- ✅ Easier to version APIs

## 🔧 FeignClient Usage

### In other services, create a simple DTO:

```java
// In learning-service or order-service
@Data
public class TransactionClientResponse {
    private Long transactionId;
    private BigDecimal amount;
    private PaymentStatus status;
    // Only fields you need
}
```

### FeignClient:
```java
@FeignClient(name = "identity-service")
public interface WalletClient {
    
    @PostMapping("/api/wallet/charge-course")
    ApiResponse<TransactionClientResponse> chargeCourse(@RequestParam BigDecimal price);
    
    @PostMapping("/api/wallet/internal/pay-order")
    ApiResponse<TransactionClientResponse> payOrder(
        @RequestParam Long userId,
        @RequestParam BigDecimal amount,
        @RequestParam(required = false) String description);
}
```

## 📝 Migration Notes

### Breaking Changes
- ⚠️ Response structure changed from Entity to DTO
- ⚠️ Some fields no longer returned (`externalTransactionId`, `orderCode`, `wallet`)

### If clients need those fields:
1. Create a separate admin endpoint with full details
2. Or add them to DTO if truly needed

### No code changes needed in:
- ✅ Service layer (still returns Entity)
- ✅ Repository layer
- ✅ Database schema

## ✅ Testing Checklist

- [ ] Test `/api/wallet/charge-course` endpoint
- [ ] Test `/api/wallet/internal/pay-order` endpoint
- [ ] Test `/api/wallet/internal/deposit-for-designer` endpoint
- [ ] Verify FeignClient calls still work
- [ ] Check that sensitive fields are not exposed
- [ ] Verify balance is correctly returned

## 🚀 Next Steps

Consider creating more specialized DTOs:
1. `TransactionSummaryResponse` - For list views (minimal fields)
2. `TransactionDetailResponse` - For detail views (more fields)
3. `TransactionAdminResponse` - For admin (all fields including sensitive ones)
