package com.sketchnotes.payment_service.clients;

import com.sketchnotes.payment_service.dtos.ApiResponse;
import com.sketchnotes.payment_service.dtos.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "identity-service"
)
public interface IdentityClient {

    @GetMapping("api/users/me")
    ApiResponse<UserResponse> getCurrentUser();
}