package com.sketchnotes.order_service.client;

import com.sketchnotes.order_service.dtos.ApiResponse;
import com.sketchnotes.order_service.dtos.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(
        name = "account-service"
)
public interface IdentityClient {

    @GetMapping("api/users/me")
    ApiResponse<UserResponse> getCurrentUser();

    @GetMapping("api/users/public/{id}")
    ApiResponse<UserResponse> getUser(@PathVariable Long id);

    @PostMapping("api/wallet/internal/deposit-for-designer")
    ApiResponse<?> depositForDesigner(
            @RequestParam Long designerId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description);
}