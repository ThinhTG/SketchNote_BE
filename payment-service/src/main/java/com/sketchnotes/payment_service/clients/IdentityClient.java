package com.sketchnotes.payment_service.clients;

import com.sketchnotes.payment_service.dtos.ApiResponse;
import com.sketchnotes.payment_service.dtos.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "identity-service",
        url = "http://localhost:8089/api/users" // hoặc qua gateway nếu có
)
public interface IdentityClient {

    @GetMapping("/me")
    ApiResponse<UserResponse> getCurrentUser();
}