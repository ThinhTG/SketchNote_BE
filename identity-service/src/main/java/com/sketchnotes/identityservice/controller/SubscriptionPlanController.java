package com.sketchnotes.identityservice.controller;

import com.sketchnotes.identityservice.dtos.request.SubscriptionPlanRequest;
import com.sketchnotes.identityservice.dtos.response.SubscriptionPlanResponse;
import com.sketchnotes.identityservice.service.interfaces.ISubscriptionPlanService;
import com.sketchnotes.identityservice.dtos.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscription-plans")
@RequiredArgsConstructor
public class SubscriptionPlanController {

    private final ISubscriptionPlanService subscriptionPlanService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> getAllActivePlans() {
        List<SubscriptionPlanResponse> plans = subscriptionPlanService.getAllActivePlans();
        return ResponseEntity.ok(ApiResponse.success(plans, "Retrieved active subscription plans"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> getPlanById(@PathVariable Long id) {
        SubscriptionPlanResponse plan = subscriptionPlanService.getPlanById(id);
        return ResponseEntity.ok(ApiResponse.success(plan, "Retrieved subscription plan"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> createPlan(
            @Valid @RequestBody SubscriptionPlanRequest request) {
        SubscriptionPlanResponse plan = subscriptionPlanService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(plan, "Subscription plan created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> updatePlan(
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionPlanRequest request) {
        SubscriptionPlanResponse plan = subscriptionPlanService.updatePlan(id, request);
        return ResponseEntity.ok(ApiResponse.success(plan, "Subscription plan updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivatePlan(@PathVariable Long id) {
        subscriptionPlanService.deactivatePlan(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Subscription plan deactivated successfully"));
    }
}
