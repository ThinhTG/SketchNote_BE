# Subscription-Based Features Implementation Guide

## 📋 Tổng quan

Tài liệu này hướng dẫn triển khai 2 tính năng liên quan đến subscription:

1. **Chức năng vẽ collab (real-time collaboration)** - Chỉ dành cho user có subscription
2. **Resource visibility trên marketplace** - Phụ thuộc vào subscription status của designer

---

## 1️⃣ Chức năng Vẽ Collab - Yêu cầu Subscription

### 🎯 Quy tắc nghiệp vụ

- ✅ **Customer/Designer CÓ subscription** → Có thể mời người khác vẽ chung
- ❌ **Customer/Designer KHÔNG CÓ subscription** → Không thể sử dụng tính năng collaboration
- ⚠️ **Free tier users** → Chỉ có thể vẽ một mình

### 📊 API đã thêm

#### Check Active Subscription
```http
GET /api/users/me/subscriptions/check
Authorization: Bearer {token}
```

**Response:**
```json
{
  "code": 200,
  "result": true,  // hoặc false
  "message": "User has active subscription"
}
```

### 🔧 Implementation Steps

#### Step 1: Thêm validation trong Project Service

**File:** `project-service/src/main/java/com/sketchnotes/project_service/service/ProjectCollaboratorService.java`

```java
@Service
@RequiredArgsConstructor
public class ProjectCollaboratorService {
    
    private final IdentityClient identityClient;
    private final ProjectCollaboratorRepository collaboratorRepository;
    
    public void inviteCollaborator(Long projectId, Long inviterId, String inviteeEmail) {
        // ✅ Check if inviter has active subscription
        boolean hasSubscription = checkUserSubscription(inviterId);
        
        if (!hasSubscription) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                "Collaboration feature requires an active subscription. Please upgrade your plan.");
        }
        
        // Continue with invitation logic...
    }
    
    private boolean checkUserSubscription(Long userId) {
        try {
            var response = identityClient.checkActiveSubscription(userId);
            return response.getResult() != null && response.getResult();
        } catch (Exception e) {
            log.error("Failed to check subscription for user {}: {}", userId, e.getMessage());
            // Fail-open: allow collaboration if service is down
            return true;
        }
    }
}
```

#### Step 2: Thêm method vào IdentityClient (Feign)

**File:** `project-service/src/main/java/com/sketchnotes/project_service/client/IdentityClient.java`

```java
@FeignClient(name = "account-service", path = "/api/users")
public interface IdentityClient {
    
    // ... existing methods ...
    
    @GetMapping("/me/subscriptions/check")
    ApiResponse<Boolean> checkActiveSubscription(@RequestHeader("X-User-Id") Long userId);
}
```

#### Step 3: Validation trong WebSocket Handler

**File:** `project-service/src/main/java/com/sketchnotes/project_service/websocket/DrawingWebSocketHandler.java`

```java
@Component
@RequiredArgsConstructor
public class DrawingWebSocketHandler {
    
    private final IdentityClient identityClient;
    
    @MessageMapping("/drawing/invite")
    public void handleInviteCollaborator(InviteMessage message, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        
        // ✅ Check subscription before allowing invite
        boolean hasSubscription = checkUserSubscription(userId);
        
        if (!hasSubscription) {
            // Send error message back to user
            messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/errors",
                new ErrorMessage("Collaboration requires an active subscription")
            );
            return;
        }
        
        // Continue with invite logic...
    }
}
```

### 🎨 Frontend Implementation

```javascript
// Check subscription before showing "Invite Collaborator" button
async function checkCollaborationAccess() {
  try {
    const response = await fetch('/api/users/me/subscriptions/check', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    const data = await response.json();
    
    if (data.result) {
      // Show "Invite Collaborator" button
      document.getElementById('invite-btn').style.display = 'block';
    } else {
      // Show upgrade message
      document.getElementById('upgrade-message').style.display = 'block';
    }
  } catch (error) {
    console.error('Failed to check subscription:', error);
  }
}
```

---

## 2️⃣ Resource Visibility - Phụ thuộc Subscription của Designer

### 🎯 Quy tắc nghiệp vụ

- ✅ **Designer CÓ subscription** → Resources hiển thị trên marketplace
- ❌ **Designer HẾT subscription** → Resources KHÔNG hiển thị trên marketplace
- 🔓 **User đã mua trước đó** → Vẫn có thể sử dụng resource đã mua (trong library của họ)
- 🔄 **Designer mua lại subscription** → Resources tự động hiển thị lại trên marketplace

### 🔧 Implementation Steps

#### Step 1: Thêm method check subscription vào IdentityClient

**File:** `order-service/src/main/java/com/sketchnotes/order_service/client/IdentityClient.java`

```java
@FeignClient(name = "account-service", path = "/api")
public interface IdentityClient {
    
    // ... existing methods ...
    
    /**
     * Check if a designer has active subscription
     * Used to filter marketplace resources
     */
    @GetMapping("/users/{userId}/subscriptions/check")
    ApiResponse<Boolean> checkUserHasActiveSubscription(@PathVariable("userId") Long userId);
}
```

#### Step 2: Cập nhật TemplateService để lọc theo subscription

**File:** `order-service/src/main/java/com/sketchnotes/order_service/service/implement/TemplateServiceImpl.java`

```java
@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {
    
    private final ResourceTemplateRepository templateRepository;
    private final IdentityClient identityClient;
    
    @Override
    public PagedResponseDTO<ResourceTemplateDTO> getAllActiveTemplates(
            int page, int size, String sortBy, String sortDir) {
        
        // Get all published templates
        Pageable pageable = PageRequest.of(page, size, 
            Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        
        Page<ResourceTemplate> templates = templateRepository
            .findByStatus(ResourceTemplate.TemplateStatus.PUBLISHED, pageable);
        
        // Filter templates by designer's subscription status
        List<ResourceTemplateDTO> filteredTemplates = templates.getContent().stream()
            .filter(template -> hasDesignerActiveSubscription(template.getDesignerId()))
            .map(this::mapToDTO)
            .collect(Collectors.toList());
        
        return PagedResponseDTO.<ResourceTemplateDTO>builder()
            .content(filteredTemplates)
            .page(page)
            .size(size)
            .totalElements((long) filteredTemplates.size())
            .totalPages((int) Math.ceil((double) filteredTemplates.size() / size))
            .first(page == 0)
            .last(page >= (filteredTemplates.size() / size))
            .hasNext(page < (filteredTemplates.size() / size))
            .hasPrevious(page > 0)
            .build();
    }
    
    /**
     * Check if designer has active subscription
     * Resources from designers without subscription won't show on marketplace
     */
    private boolean hasDesignerActiveSubscription(Long designerId) {
        try {
            var response = identityClient.checkUserHasActiveSubscription(designerId);
            return response.getResult() != null && response.getResult();
        } catch (Exception e) {
            log.warn("Failed to check subscription for designer {}: {}", 
                designerId, e.getMessage());
            // Fail-open: show resources if service is down
            return true;
        }
    }
}
```

#### Step 3: Cập nhật tất cả API marketplace

Áp dụng logic tương tự cho các API:
- `getTemplatesByType()`
- `searchTemplates()`
- `getPopularTemplates()`
- `getLatestTemplates()`
- `getTemplatesByPriceRange()`

#### Step 4: User Library - KHÔNG lọc theo subscription

**File:** `order-service/src/main/java/com/sketchnotes/order_service/service/implement/UserResourceServiceImpl.java`

```java
@Override
public List<ResourceTemplateDTO> getPurchasedTemplates(Long userId) {
    List<Long> templateIds = userResourceRepository.findActiveTemplateIdsByUserId(userId);
    
    if (templateIds == null || templateIds.isEmpty()) {
        return Collections.emptyList();
    }

    // ✅ Get ALL purchased templates regardless of designer's subscription
    // User đã mua thì vẫn có quyền sử dụng
    List<ResourceTemplate> templates = resourceTemplateRepository
        .findByTemplateIdInAndStatus(templateIds, ResourceTemplate.TemplateStatus.PUBLISHED);
    
    return templates.stream()
        .map(this::mapToDTO)
        .collect(Collectors.toList());
}
```

### 📊 Database Query Optimization

Để tránh N+1 query problem, có thể tạo custom query:

**File:** `order-service/src/main/java/com/sketchnotes/order_service/repository/ResourceTemplateRepository.java`

```java
public interface ResourceTemplateRepository extends JpaRepository<ResourceTemplate, Long> {
    
    /**
     * Find templates with designer subscription check
     * This is a placeholder - actual implementation would require:
     * 1. Join with identity-service data (via API or event-driven cache)
     * 2. Or use Redis cache to store designer subscription status
     */
    @Query("""
        SELECT t FROM ResourceTemplate t 
        WHERE t.status = :status 
        AND t.designerId IN :activeDesignerIds
        ORDER BY t.createdAt DESC
    """)
    Page<ResourceTemplate> findByStatusAndActiveDesigners(
        @Param("status") ResourceTemplate.TemplateStatus status,
        @Param("activeDesignerIds") List<Long> activeDesignerIds,
        Pageable pageable
    );
}
```

### 🚀 Performance Optimization với Redis Cache

**File:** `order-service/src/main/java/com/sketchnotes/order_service/service/DesignerSubscriptionCacheService.java`

```java
@Service
@RequiredArgsConstructor
public class DesignerSubscriptionCacheService {
    
    private final RedisTemplate<String, Boolean> redisTemplate;
    private final IdentityClient identityClient;
    
    private static final String CACHE_PREFIX = "designer:subscription:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);
    
    public boolean hasActiveSubscription(Long designerId) {
        String cacheKey = CACHE_PREFIX + designerId;
        
        // Try cache first
        Boolean cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // Fetch from identity-service
        boolean hasSubscription = false;
        try {
            var response = identityClient.checkUserHasActiveSubscription(designerId);
            hasSubscription = response.getResult() != null && response.getResult();
        } catch (Exception e) {
            log.error("Failed to check subscription for designer {}", designerId, e);
            hasSubscription = true; // Fail-open
        }
        
        // Cache result
        redisTemplate.opsForValue().set(cacheKey, hasSubscription, CACHE_TTL);
        
        return hasSubscription;
    }
    
    /**
     * Invalidate cache when designer purchases/cancels subscription
     */
    public void invalidateCache(Long designerId) {
        String cacheKey = CACHE_PREFIX + designerId;
        redisTemplate.delete(cacheKey);
    }
}
```

### 📡 Event-Driven Cache Invalidation

**File:** `identity-service/src/main/java/com/sketchnotes/identityservice/service/UserSubscriptionService.java`

```java
@Service
@RequiredArgsConstructor
public class UserSubscriptionService {
    
    private final StreamBridge streamBridge;
    
    @Override
    @Transactional
    public UserSubscriptionResponse purchaseSubscription(Long userId, PurchaseSubscriptionRequest request) {
        // ... existing purchase logic ...
        
        // Publish event to invalidate cache
        SubscriptionChangedEvent event = SubscriptionChangedEvent.builder()
            .userId(userId)
            .hasActiveSubscription(true)
            .timestamp(LocalDateTime.now())
            .build();
        
        streamBridge.send("subscriptionChanged-out-0", event);
        
        return mapToResponse(savedSubscription);
    }
    
    @Override
    @Scheduled(cron = "0 0 0 * * *")
    public void processExpiredSubscriptions() {
        List<UserSubscription> expiredSubscriptions = 
            userSubscriptionRepository.findExpiredSubscriptions(LocalDateTime.now());
        
        for (UserSubscription subscription : expiredSubscriptions) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            userSubscriptionRepository.save(subscription);
            
            // Publish event
            SubscriptionChangedEvent event = SubscriptionChangedEvent.builder()
                .userId(subscription.getUser().getId())
                .hasActiveSubscription(false)
                .timestamp(LocalDateTime.now())
                .build();
            
            streamBridge.send("subscriptionChanged-out-0", event);
        }
    }
}
```

**File:** `order-service/src/main/java/com/sketchnotes/order_service/consumer/SubscriptionEventConsumer.java`

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionEventConsumer {
    
    private final DesignerSubscriptionCacheService cacheService;
    
    @Bean
    public Consumer<SubscriptionChangedEvent> subscriptionChanged() {
        return event -> {
            log.info("Received subscription changed event for user {}", event.getUserId());
            cacheService.invalidateCache(event.getUserId());
        };
    }
}
```

---

## 🧪 Testing Guide

### Test 1: Collaboration với subscription
```bash
# User có subscription
POST /api/projects/{projectId}/collaborators
Authorization: Bearer {token_with_subscription}
{
  "inviteeEmail": "user@example.com"
}
# Expected: 200 OK

# User không có subscription
POST /api/projects/{projectId}/collaborators
Authorization: Bearer {token_without_subscription}
{
  "inviteeEmail": "user@example.com"
}
# Expected: 403 FORBIDDEN
```

### Test 2: Marketplace visibility
```bash
# Designer có subscription → Resources hiển thị
GET /api/orders/template
# Response: Bao gồm resources của designer có subscription

# Designer hết subscription → Resources KHÔNG hiển thị
# (Manually expire subscription in DB)
GET /api/orders/template
# Response: KHÔNG bao gồm resources của designer hết hạn

# User library - Vẫn thấy resource đã mua
GET /api/orders/user_resources/user/me/templates
# Response: Bao gồm TẤT CẢ resources đã mua, kể cả từ designer hết hạn
```

### Test 3: Designer renew subscription
```bash
# Designer mua lại subscription
POST /api/users/me/subscriptions
{
  "planId": 3,
  "autoRenew": false
}

# Check marketplace - Resources hiển thị lại
GET /api/orders/template
# Response: Resources của designer xuất hiện lại
```

---

## 📝 Summary

### ✅ Đã implement:
1. ✅ API check active subscription: `GET /api/users/me/subscriptions/check`
2. ✅ Method `hasActiveSubscription()` trong `UserSubscriptionService`

### 🚧 Cần implement:
1. ⏳ Validation collaboration trong Project Service
2. ⏳ Filter marketplace theo designer subscription
3. ⏳ Redis cache cho performance
4. ⏳ Event-driven cache invalidation
5. ⏳ Frontend UI cho subscription check

### 🎯 Next Steps:
1. Implement validation trong Project Service
2. Cập nhật TemplateService để lọc theo subscription
3. Thêm Redis cache (optional nhưng recommended)
4. Test end-to-end flow
5. Update frontend để check subscription trước khi hiển thị collaboration features

---

## 📚 Related Files

### Identity Service:
- `service/interfaces/IUserSubscriptionService.java` - Interface với method mới
- `service/UserSubscriptionService.java` - Implementation
- `controller/UserSubscriptionController.java` - API endpoint

### Order Service (Cần update):
- `client/IdentityClient.java` - Thêm method check subscription
- `service/implement/TemplateServiceImpl.java` - Filter marketplace
- `service/implement/UserResourceServiceImpl.java` - User library (không filter)

### Project Service (Cần implement):
- `service/ProjectCollaboratorService.java` - Validation
- `client/IdentityClient.java` - Feign client
- `websocket/DrawingWebSocketHandler.java` - WebSocket validation
