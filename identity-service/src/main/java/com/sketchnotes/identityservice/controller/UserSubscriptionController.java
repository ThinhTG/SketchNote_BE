package com.sketchnotes.identityservice.controller;

import com.sketchnotes.identityservice.dtos.request.PurchaseSubscriptionRequest;
import com.sketchnotes.identityservice.dtos.response.UserQuotaResponse;
import com.sketchnotes.identityservice.dtos.response.UserSubscriptionResponse;
import com.sketchnotes.identityservice.service.interfaces.IUserSubscriptionService;
import com.sketchnotes.identityservice.dtos.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/me/subscriptions")
@RequiredArgsConstructor
public class UserSubscriptionController {

    private final IUserSubscriptionService userSubscriptionService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserSubscriptionResponse>> purchaseSubscription(
            Authentication authentication,
            @Valid @RequestBody PurchaseSubscriptionRequest request) {
        Long userId = getUserIdFromAuth(authentication);
        UserSubscriptionResponse subscription = userSubscriptionService.purchaseSubscription(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(subscription, "Subscription purchased successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserSubscriptionResponse>>> getMySubscriptions(
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        List<UserSubscriptionResponse> subscriptions = userSubscriptionService.getUserSubscriptions(userId);
        return ResponseEntity.ok(ApiResponse.success(subscriptions, "Retrieved user subscriptions"));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<UserSubscriptionResponse>> getActiveSubscription(
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        UserSubscriptionResponse subscription = userSubscriptionService.getActiveSubscription(userId);
        return ResponseEntity.ok(ApiResponse.success(subscription, "Retrieved active subscription"));
    }

    @DeleteMapping("/{subscriptionId}")
    public ResponseEntity<ApiResponse<Void>> cancelSubscription(
            Authentication authentication,
            @PathVariable Long subscriptionId) {
        Long userId = getUserIdFromAuth(authentication);
        userSubscriptionService.cancelSubscription(userId, subscriptionId);
        return ResponseEntity.ok(ApiResponse.success(null, "Subscription cancelled successfully"));
    }

    @GetMapping("/quota")
    public ResponseEntity<ApiResponse<UserQuotaResponse>> getMyQuota(
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        UserQuotaResponse quota = userSubscriptionService.getUserQuota(userId);
        return ResponseEntity.ok(ApiResponse.success(quota, "Retrieved user quota"));
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            return Long.parseLong(jwt.getClaim("userId"));
        }
        throw new RuntimeException("Unable to extract user ID from authentication");
    }
}
