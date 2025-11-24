package com.sketchnotes.order_service.client;

import com.sketchnotes.order_service.dtos.ApiResponse;
import com.sketchnotes.order_service.dtos.UserResponse;
import com.sketchnotes.order_service.dtos.TransactionResponse;
import com.sketchnotes.order_service.dtos.TransactionType;
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

    @GetMapping("/api/users/me")
    ApiResponse<UserResponse> getCurrentUser();

    @GetMapping("/api/users/public/{id}")
    ApiResponse<UserResponse> getUser(@PathVariable Long id);

    @PostMapping("/api/wallet/charge-course")
    ApiResponse<TransactionResponse> chargeCourse(
            @RequestParam Long userId,
            @RequestParam double price,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "COURSE_FEE") TransactionType type
    );

    @GetMapping("/api/admin/stats/users")
    java.util.Map<String, Long> getUserStats();

    @GetMapping("/api/admin/stats/course-revenue")
    java.util.List<java.util.Map<String, Object>> getCourseRevenue(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam String groupBy
    );

    @PostMapping("/api/wallet/internal/deposit-for-designer")
    ApiResponse<?> depositForDesigner(
            @RequestParam Long designerId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description);

    @PostMapping("/api/wallet/internal/pay-order")
    ApiResponse<?> payOrderFromWallet(
            @RequestParam Long userId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description);
}