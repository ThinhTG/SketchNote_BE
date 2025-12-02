# HƯỚNG DẪN NHANH: Tích hợp Credit System

## ⚡ Bước 1: Thêm vào AuthenticationService.java

### 1.1. Thêm import (sau dòng 18)

Tìm dòng:
```java
import com.sketchnotes.identityservice.service.interfaces.IWalletService;
```

Thêm ngay sau đó:
```java
import com.sketchnotes.identityservice.events.UserCreatedEvent;
```

Tìm dòng:
```java
import org.springframework.beans.factory.annotation.Value;
```

Thêm ngay sau đó:
```java
import org.springframework.context.ApplicationEventPublisher;
```

### 1.2. Thêm dependency (sau dòng 40)

Tìm dòng:
```java
private  final KafkaProducerService kafkaProducerService;
```

Thêm ngay sau đó:
```java
private final ApplicationEventPublisher eventPublisher;
```

### 1.3. Publish event trong loginWithGoogle() (sau dòng 186)

Tìm đoạn code:
```java
// Create wallet for new user
try {
    walletService.createWallet(user.getId());
    log.info("Wallet created for new user: {}", user.getId());
} catch (Exception ex) {
    log.error("Failed to create wallet for user: {}", user.getId(), ex);
    // Continue even if wallet creation fails
}

log.info("Successfully created new user with ID: {}", user.getId());
```

Thay bằng:
```java
// Create wallet for new user
try {
    walletService.createWallet(user.getId());
    log.info("Wallet created for new user: {}", user.getId());
} catch (Exception ex) {
    log.error("Failed to create wallet for user: {}", user.getId(), ex);
    // Continue even if wallet creation fails
}

// Publish UserCreatedEvent to trigger credit grant
try {
    UserCreatedEvent event = new UserCreatedEvent(this, user.getId(), user.getEmail());
    eventPublisher.publishEvent(event);
    log.info("Published UserCreatedEvent for user: {}", user.getId());
} catch (Exception ex) {
    log.error("Failed to publish UserCreatedEvent for user: {}", user.getId(), ex);
}

log.info("Successfully created new user with ID: {}", user.getId());
```

### 1.4. Publish event trong register() (sau dòng 256)

Tìm đoạn code:
```java
user = userRepository.save(user);
walletService.createWallet(user.getId());
```

Thay bằng:
```java
user = userRepository.save(user);
walletService.createWallet(user.getId());

// Publish UserCreatedEvent to trigger credit grant
try {
    UserCreatedEvent event = new UserCreatedEvent(this, user.getId(), user.getEmail());
    eventPublisher.publishEvent(event);
    log.info("Published UserCreatedEvent for user: {}", user.getId());
} catch (Exception ex) {
    log.error("Failed to publish UserCreatedEvent for user: {}", user.getId(), ex);
}
```

---

## ⚡ Bước 2: Tạo AsyncConfiguration.java

Tạo file mới: `identity-service/src/main/java/com/sketchnotes/identityservice/config/AsyncConfiguration.java`

```java
package com.sketchnotes.identityservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfiguration {
    // Async processing enabled for event listeners
}
```

---

## ⚡ Bước 3: Run Migration

Migration sẽ tự động chạy khi bạn start application (Flyway).

File migration đã được tạo sẵn tại:
`identity-service/src/main/resources/db/migration/V5__add_ai_credits_system.sql`

---

## ⚡ Bước 4: Test

### 4.1. Start services
```bash
# Start identity-service
# Start project-service
```

### 4.2. Test với Postman

1. Import collection từ: `.docs/AI_Credits_Postman_Collection.json`

2. Test flow:
   - Register user mới
   - Login
   - Check credit balance (should be 50)
   - Generate image (cost 10 credits)
   - Check balance again (should be 40)
   - View history

---

## ✅ Checklist

- [ ] Thêm import `UserCreatedEvent`
- [ ] Thêm import `ApplicationEventPublisher`
- [ ] Thêm dependency `eventPublisher`
- [ ] Publish event trong `loginWithGoogle()`
- [ ] Publish event trong `register()`
- [ ] Tạo `AsyncConfiguration.java`
- [ ] Start application (migration tự động chạy)
- [ ] Test với Postman

---

## 🐛 Troubleshooting

Nếu gặp lỗi compile:
1. Check tất cả imports đã đúng
2. Check `eventPublisher` đã được inject
3. Rebuild project

Nếu user không nhận được credits:
1. Check logs xem có "Published UserCreatedEvent" không
2. Check logs xem có "Successfully granted 50 initial credits" không
3. Query database: `SELECT * FROM credit_transactions WHERE type = 'INITIAL_BONUS'`

---

**Lưu ý**: Nếu bạn gặp khó khăn khi sửa file, tôi có thể tạo một file AuthenticationService.java hoàn chỉnh để bạn thay thế toàn bộ.
