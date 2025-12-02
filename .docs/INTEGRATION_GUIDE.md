# Hướng dẫn tích hợp UserCreatedEvent vào AuthenticationService

## Bước 1: Import các class cần thiết

Thêm vào đầu file `AuthenticationService.java`:

```java
import com.sketchnotes.identityservice.events.UserCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;
```

## Bước 2: Inject ApplicationEventPublisher

Thêm vào constructor dependencies:

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService implements IAuthService {
    private final IdentityClient identityClient;
    private final IUserRepository userRepository;
    private final ErrorNormalizer errorNormalizer;
    private final IWalletService walletService;
    private final ICreditService creditService;  // <-- Thêm dòng này
    private final KafkaProducerService kafkaProducerService;
    private final ApplicationEventPublisher eventPublisher;  // <-- Thêm dòng này
    
    // ... rest of the code
}
```

## Bước 3: Publish event sau khi tạo user trong method `loginWithGoogle()`

Tìm đoạn code tạo user mới trong method `loginWithGoogle()` (khoảng dòng 164-189):

```java
if (user == null) {
    log.info("Creating new user from Google OAuth: {}", email);
    
    User newUser = User.builder()
            .keycloakId(keycloakId)
            .email(email)
            .firstName(firstName != null ? firstName : "")
            .lastName(lastName != null ? lastName : "")
            .role(Role.CUSTOMER)
            .isActive(true)
            .createAt(LocalDateTime.now())
            .build();
    
    user = userRepository.save(newUser);
    
    // Create wallet for new user
    try {
        walletService.createWallet(user.getId());
        log.info("Wallet created for new user: {}", user.getId());
    } catch (Exception ex) {
        log.error("Failed to create wallet for user: {}", user.getId(), ex);
    }
    
    // ========== THÊM ĐOẠN CODE NÀY ==========
    // Publish UserCreatedEvent để trigger credit grant
    try {
        UserCreatedEvent event = new UserCreatedEvent(this, user.getId(), user.getEmail());
        eventPublisher.publishEvent(event);
        log.info("Published UserCreatedEvent for user: {}", user.getId());
    } catch (Exception ex) {
        log.error("Failed to publish UserCreatedEvent for user: {}", user.getId(), ex);
    }
    // ========================================
    
    log.info("Successfully created new user with ID: {}", user.getId());
}
```

## Bước 4: Publish event sau khi tạo user trong method `register()`

Tìm đoạn code tạo user mới trong method `register()` (khoảng dòng 245-256):

```java
User user = User.builder()
        .keycloakId(userId)
        .email(request.getEmail())
        .firstName(request.getFirstName())
        .lastName(request.getLastName())
        .createAt(LocalDateTime.now())
        .role(Role.CUSTOMER)
        .isActive(true)
        .avatarUrl(request.getAvatarUrl())
        .build();
user = userRepository.save(user);
walletService.createWallet(user.getId());

// ========== THÊM ĐOẠN CODE NÀY ==========
// Publish UserCreatedEvent để trigger credit grant
try {
    UserCreatedEvent event = new UserCreatedEvent(this, user.getId(), user.getEmail());
    eventPublisher.publishEvent(event);
    log.info("Published UserCreatedEvent for user: {}", user.getId());
} catch (Exception ex) {
    log.error("Failed to publish UserCreatedEvent for user: {}", user.getId(), ex);
}
// ========================================
```

## Bước 5: Enable Async Processing (Optional nhưng khuyến nghị)

Tạo file `AsyncConfiguration.java` trong package `config`:

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

## Tổng kết

Sau khi hoàn thành các bước trên:

1. **User mới đăng ký** → `register()` được gọi
2. **User được tạo** trong database
3. **Wallet được tạo** cho user
4. **UserCreatedEvent được publish**
5. **UserCreatedEventListener** nhận event (async)
6. **50 credits miễn phí** được tự động tặng cho user

## Test

Sau khi thêm code, test bằng cách:

1. Đăng ký user mới
2. Kiểm tra credit balance:
   ```http
   GET /api/credits/balance
   Authorization: Bearer {token}
   ```
3. Kết quả mong đợi: `currentBalance: 50`

## Lưu ý

- Event được xử lý **async** nên không block registration flow
- Nếu credit grant fail, user vẫn được tạo thành công
- Check logs để debug nếu có vấn đề:
  ```
  Published UserCreatedEvent for user: 123
  Handling UserCreatedEvent for user: 123
  Successfully granted 50 initial credits to user: 123
  ```
