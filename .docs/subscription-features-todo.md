# Subscription Features - TODO List

## ✅ Completed

### Identity Service
- [x] Add `hasActiveSubscription(Long userId)` method to `IUserSubscriptionService`
- [x] Implement `hasActiveSubscription()` in `UserSubscriptionService`
- [x] Add API endpoint `GET /api/users/me/subscriptions/check`
- [x] Compile and test identity-service

---

## 🚧 In Progress / TODO

### 1. Collaboration Feature - Require Subscription

#### Project Service
- [ ] Add `IdentityClient.checkActiveSubscription()` Feign method
- [ ] Implement validation in `ProjectCollaboratorService.inviteCollaborator()`
- [ ] Add validation in WebSocket handler (`DrawingWebSocketHandler`)
- [ ] Add error handling and user-friendly messages
- [ ] Write unit tests for collaboration validation

#### Frontend
- [ ] Call `GET /api/users/me/subscriptions/check` on page load
- [ ] Show/hide "Invite Collaborator" button based on subscription
- [ ] Display upgrade message for users without subscription
- [ ] Handle WebSocket errors when trying to invite without subscription

---

### 2. Marketplace Visibility - Designer Subscription

#### Order Service - IdentityClient
- [ ] Add method `checkUserHasActiveSubscription(Long userId)` to IdentityClient
- [ ] Test Feign client connection

#### Order Service - TemplateService
- [ ] Update `getAllActiveTemplates()` to filter by designer subscription
- [ ] Update `getTemplatesByType()` to filter by designer subscription
- [ ] Update `searchTemplates()` to filter by designer subscription
- [ ] Update `getPopularTemplates()` to filter by designer subscription
- [ ] Update `getLatestTemplates()` to filter by designer subscription
- [ ] Update `getTemplatesByPriceRange()` to filter by designer subscription

#### Order Service - UserResourceService
- [ ] Verify `getPurchasedTemplates()` does NOT filter by subscription
- [ ] Ensure users can access all purchased resources regardless of designer status

#### Testing
- [ ] Test marketplace with active designer subscription
- [ ] Test marketplace with expired designer subscription
- [ ] Test user library with expired designer subscription
- [ ] Test designer renew subscription → resources reappear

---

### 3. Performance Optimization (Optional)

#### Redis Cache
- [ ] Create `DesignerSubscriptionCacheService`
- [ ] Implement cache with 5-minute TTL
- [ ] Add cache invalidation logic
- [ ] Configure Redis connection in application.yml

#### Event-Driven Architecture
- [ ] Create `SubscriptionChangedEvent` class
- [ ] Publish event when subscription is purchased
- [ ] Publish event when subscription expires
- [ ] Create Kafka consumer in order-service
- [ ] Implement cache invalidation on event

#### Batch Processing
- [ ] Implement batch check for multiple designers
- [ ] Optimize database queries to avoid N+1 problem

---

### 4. Documentation & Testing

#### Documentation
- [x] Create implementation guide
- [x] Create summary document
- [ ] Update API documentation (Swagger/OpenAPI)
- [ ] Create sequence diagrams
- [ ] Update README with new features

#### Testing
- [ ] Unit tests for subscription check
- [ ] Integration tests for collaboration validation
- [ ] Integration tests for marketplace filtering
- [ ] E2E tests for complete user flows
- [ ] Performance tests for marketplace with 1000+ templates
- [ ] Load tests for concurrent subscription checks

---

### 5. Frontend Implementation

#### Collaboration UI
- [ ] Add subscription check on project page load
- [ ] Show/hide "Invite" button
- [ ] Display "Upgrade to Pro" message
- [ ] Add link to subscription purchase page
- [ ] Handle WebSocket errors gracefully

#### Marketplace UI
- [ ] No changes needed (filtering happens server-side)
- [ ] Verify resources appear/disappear correctly

#### User Library UI
- [ ] No changes needed
- [ ] Verify all purchased resources always visible

---

## 📊 Progress Tracking

### Overall Progress: 10%

- ✅ Identity Service API: **100%** (4/4 tasks)
- ⏳ Collaboration Feature: **0%** (0/9 tasks)
- ⏳ Marketplace Filtering: **0%** (0/13 tasks)
- ⏳ Performance Optimization: **0%** (0/9 tasks)
- ⏳ Documentation & Testing: **33%** (2/6 tasks)
- ⏳ Frontend: **0%** (0/7 tasks)

---

## 🎯 Sprint Planning

### Sprint 1: Core Functionality (Week 1)
**Goal:** Implement basic subscription checks without optimization

Tasks:
1. ✅ Identity Service API
2. Collaboration validation in Project Service
3. Marketplace filtering in Order Service
4. Basic testing

**Deliverable:** Working subscription-based features

### Sprint 2: Optimization (Week 2)
**Goal:** Improve performance and user experience

Tasks:
1. Redis cache implementation
2. Event-driven cache invalidation
3. Batch processing
4. Performance testing

**Deliverable:** Optimized system with <500ms response time

### Sprint 3: Polish & Documentation (Week 3)
**Goal:** Complete testing and documentation

Tasks:
1. Comprehensive testing (unit, integration, E2E)
2. API documentation
3. Frontend implementation
4. User guides

**Deliverable:** Production-ready features with full documentation

---

## 🐛 Known Issues / Risks

### Risks
1. **Identity Service Downtime**
   - Mitigation: Fail-open strategy implemented
   - Impact: Low (features still work, just not enforced)

2. **Performance with Large Dataset**
   - Mitigation: Redis cache + batch processing
   - Impact: Medium (slow marketplace loading)

3. **Cache Inconsistency**
   - Mitigation: Event-driven invalidation
   - Impact: Low (5-minute max delay)

### Issues
- None currently

---

## 📝 Notes

### Design Decisions
1. **Fail-Open vs Fail-Closed**
   - Decision: Fail-open (allow access if service down)
   - Reason: Better UX, availability > strict enforcement

2. **User Library Filtering**
   - Decision: No filtering for purchased resources
   - Reason: Users paid for it, should always have access

3. **Cache TTL**
   - Decision: 5 minutes
   - Reason: Balance between freshness and performance

### Future Enhancements
- [ ] Subscription tier-based collaboration limits (e.g., max 5 collaborators)
- [ ] Analytics dashboard for subscription impact
- [ ] A/B testing for subscription prompts
- [ ] Automated subscription renewal reminders

---

## 🔗 Quick Links

- [Implementation Guide](./subscription-features-implementation.md)
- [Summary](./subscription-features-summary.md)
- [Subscription Guide](../identity-service/SUBSCRIPTION_GUIDE.md)
- [API Documentation](#) (TODO)
