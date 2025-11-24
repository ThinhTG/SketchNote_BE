package com.sketchnotes.order_service.controller;

import com.sketchnotes.order_service.dtos.ApiResponse;
import com.sketchnotes.order_service.entity.UserResource;
import com.sketchnotes.order_service.service.UserResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for inter-service communication via FeignClient
 * This provides endpoints specifically designed for other microservices to call
 */
@RestController
@RequestMapping("/api/user-resources")
@RequiredArgsConstructor
public class UserResourceFeignController {
    
    private final UserResourceService userResourceService;
    
    /**
     * Check if user has purchased a specific resource
     * This endpoint is used by identity-service to validate feedback eligibility
     * 
     * @param userId the user ID
     * @param resourceId the resource template ID
     * @return UserResource if found and active
     * @throws IllegalArgumentException if user has not purchased the resource or it's not active
     */
    @GetMapping("/user/{userId}/resource/{resourceId}")
    public ResponseEntity<ApiResponse<UserResource>> getUserResource(
            @PathVariable Long userId,
            @PathVariable Long resourceId) {
        UserResource userResource = userResourceService.getUserResourceByUserIdAndResourceId(userId, resourceId);
        return ResponseEntity.ok(ApiResponse.success(userResource, "User resource retrieved successfully"));
    }
}
