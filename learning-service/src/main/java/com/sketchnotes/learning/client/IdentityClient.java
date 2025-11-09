package com.sketchnotes.learning.client;

import com.sketchnotes.learning.dto.ApiResponse;
import com.sketchnotes.learning.dto.response.UserResponse;
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

    @PostMapping("api/wallet/charge-course")
    ApiResponse<TransactionResponse> chargeCourse(
            @RequestParam Long userId,
            @RequestParam double price,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "COURSE_PAYMENT") TransactionType type
    );
}