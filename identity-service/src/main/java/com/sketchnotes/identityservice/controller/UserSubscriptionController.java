package com.sketchnotes.identityservice.controller;

import com.sketchnotes.identityservice.dtos.request.PurchaseSubscriptionRequest;
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
}
