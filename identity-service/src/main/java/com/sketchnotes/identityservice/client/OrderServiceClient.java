package com.sketchnotes.identityservice.client;

import com.sketchnotes.identityservice.dtos.ApiResponse;
import com.sketchnotes.identityservice.dtos.response.UserResourceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "order-service", url = "${order-service.url:http://localhost:8083}")
public interface OrderServiceClient {
    
    /**
     * Check if user has purchased a specific resource
     * Used to validate if user can submit feedback for a resource
     */
    @GetMapping("/api/user-resources/user/{userId}/resource/{resourceId}")
    ApiResponse<UserResourceResponse> getUserResource(
            @PathVariable("userId") Long userId,
            @PathVariable("resourceId") Long resourceId
    );
}
