package com.sketchnotes.identityservice.controller;

import com.sketchnotes.identityservice.dtos.ApiResponse;
import com.sketchnotes.identityservice.dtos.request.WithdrawalRequestDto;
import com.sketchnotes.identityservice.dtos.response.WithdrawalResponse;
import com.sketchnotes.identityservice.service.interfaces.IUserService;
import com.sketchnotes.identityservice.service.interfaces.IWithdrawalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for customer withdrawal operations.
 */
@RestController
@RequestMapping("/api/withdraw")
@RequiredArgsConstructor
@Slf4j
public class WithdrawalController {
    
    private final IWithdrawalService withdrawalService;
    private final IUserService userService;
    
    /**
     * Customer creates a withdrawal request.
     * POST /api/withdraw/request
     */
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<WithdrawalResponse>> createWithdrawalRequest(
            @Valid @RequestBody WithdrawalRequestDto request) {
        log.info("Withdrawal request received: {}", request);
        
        Long userId = userService.getCurrentUser().getId();
        WithdrawalResponse response = withdrawalService.createWithdrawalRequest(userId, request);
        
        return ResponseEntity.ok(ApiResponse.success(
                response,
                "Withdrawal request submitted successfully. Your money will be sent to your bank account within 24 hours."
        ));
    }
    
    /**
     * Customer gets their withdrawal history.
     * GET /api/withdraw/my-history
     */
    @GetMapping("/my-history")
    public ResponseEntity<ApiResponse<List<WithdrawalResponse>>> getMyWithdrawalHistory() {
        Long userId = userService.getCurrentUser().getId();
        List<WithdrawalResponse> response = withdrawalService.getWithdrawalHistory(userId);
        
        return ResponseEntity.ok(ApiResponse.success(
                response,
                "Withdrawal history retrieved successfully"
        ));
    }
}
