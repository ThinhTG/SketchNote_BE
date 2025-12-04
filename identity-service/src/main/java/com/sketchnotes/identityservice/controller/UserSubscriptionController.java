package com.sketchnotes.identityservice.controller;

import com.sketchnotes.identityservice.dtos.request.PurchaseSubscriptionRequest;
import com.sketchnotes.identityservice.dtos.response.SubscriptionUpgradeCheckResponse;
import com.sketchnotes.identityservice.dtos.response.UserQuotaResponse;
import com.sketchnotes.identityservice.dtos.response.UserSubscriptionResponse;
import com.sketchnotes.identityservice.service.interfaces.IUserSubscriptionService;
import com.sketchnotes.identityservice.service.interfaces.IUserService;
import com.sketchnotes.identityservice.dtos.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/me/subscriptions")
@RequiredArgsConstructor
public class UserSubscriptionController {

    private final IUserSubscriptionService userSubscriptionService;
    private final IUserService userService;

    /**
     * Check if user can upgrade to a new plan
     * Returns warning if user has an active subscription that will be replaced
     * 
     * GET /api/users/me/subscriptions/check-upgrade?planId=2
     */
    @GetMapping("/check-upgrade")
    public ResponseEntity<ApiResponse<SubscriptionUpgradeCheckResponse>> checkUpgrade(
            @RequestParam Long planId) {
        Long userId = userService.getCurrentUser().getId();
        SubscriptionUpgradeCheckResponse response = userSubscriptionService.checkUpgrade(userId, planId);
        return ResponseEntity.ok(ApiResponse.success(response, "Upgrade check completed"));
    }

    /**
     * Purchase a subscription plan
     * 
     * If user has an active subscription:
     * - First call checkUpgrade to see warning
     * - Then call this endpoint with confirmUpgrade=true to proceed
     * 
     * POST /api/users/me/subscriptions
     * Body: { "planId": 2, "autoRenew": false, "confirmUpgrade": true }
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserSubscriptionResponse>> purchaseSubscription(
            @Valid @RequestBody PurchaseSubscriptionRequest request) {
        Long userId = userService.getCurrentUser().getId();
        UserSubscriptionResponse subscription = userSubscriptionService.purchaseSubscription(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(subscription, "Subscription purchased successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserSubscriptionResponse>>> getMySubscriptions() {
        Long userId = userService.getCurrentUser().getId();
        List<UserSubscriptionResponse> subscriptions = userSubscriptionService.getUserSubscriptions(userId);
        return ResponseEntity.ok(ApiResponse.success(subscriptions, "Retrieved user subscriptions"));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<UserSubscriptionResponse>> getActiveSubscription() {
        Long userId = userService.getCurrentUser().getId();
        UserSubscriptionResponse subscription = userSubscriptionService.getActiveSubscription(userId);
        return ResponseEntity.ok(ApiResponse.success(subscription, "Retrieved active subscription"));
    }

    @DeleteMapping("/{subscriptionId}")
    public ResponseEntity<ApiResponse<Void>> cancelSubscription(
            @PathVariable Long subscriptionId) {
        Long userId = userService.getCurrentUser().getId();
        userSubscriptionService.cancelSubscription(userId, subscriptionId);
        return ResponseEntity.ok(ApiResponse.success(null, "Subscription cancelled successfully"));
    }

    @GetMapping("/quota")
    public ResponseEntity<ApiResponse<UserQuotaResponse>> getMyQuota() {
        Long userId = userService.getCurrentUser().getId();
        UserQuotaResponse quota = userSubscriptionService.getUserQuota(userId);
        return ResponseEntity.ok(ApiResponse.success(quota, "Retrieved user quota"));
    }
    
    /**
     * Check if current user has active subscription (for collaboration feature)
     */
    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Boolean>> checkActiveSubscription() {
        Long userId = userService.getCurrentUser().getId();
        boolean hasActive = userSubscriptionService.hasActiveSubscription(userId);
        return ResponseEntity.ok(ApiResponse.success(hasActive, 
            hasActive ? "User has active subscription" : "User does not have active subscription"));
    }
}
