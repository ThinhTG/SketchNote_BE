package com.sketchnotes.order_service.client;

import com.sketchnotes.order_service.dtos.ApiResponse;
import com.sketchnotes.order_service.dtos.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "identity-service",
        url = "http://localhost:8089/"
)
public interface IdentityClient {

    @GetMapping("api/users/me")
    ApiResponse<UserResponse> getCurrentUser();
}