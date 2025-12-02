package com.sketchnotes.project_service.client;

import com.sketchnotes.project_service.dtos.ApiResponse;
import com.sketchnotes.project_service.dtos.request.UseCreditRequest;
import com.sketchnotes.project_service.dtos.response.CreditBalanceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Feign Client để gọi Credit API từ Identity Service
 */
@FeignClient(name = "identity-service", path = "/api/credits", contextId = "creditClient")
public interface CreditClient {
    
    /**
     * Sử dụng credit (trừ credit từ user)
     */
    @PostMapping("/use")
    ResponseEntity<ApiResponse<CreditBalanceResponse>> useCredits(@RequestBody UseCreditRequest request);
    
    /**
     * Kiểm tra xem user có đủ credit không
     */
    @GetMapping("/check")
    ResponseEntity<ApiResponse<Boolean>> checkCredits(
            @RequestParam("userId") Long userId,
            @RequestParam("amount") Integer amount);
}
