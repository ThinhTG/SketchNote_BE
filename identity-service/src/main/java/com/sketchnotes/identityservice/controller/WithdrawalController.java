package com.sketchnotes.identityservice.controller;

import com.sketchnotes.identityservice.dtos.ApiResponse;
import com.sketchnotes.identityservice.dtos.request.WithdrawalRequestDto;
import com.sketchnotes.identityservice.dtos.response.WithdrawalResponse;
import com.sketchnotes.identityservice.service.interfaces.IUserService;
import com.sketchnotes.identityservice.service.interfaces.IWithdrawalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
     * Customer gets their withdrawal history with pagination.
     * GET /api/withdraw/my-history?page=0&size=10&sort=createdAt,desc
     * 
     * @param page Page number (default: 0)
     * @param size Page size (default: 10)
     * @param sortBy Sort field (default: createdAt)
     * @param sortDirection Sort direction (default: desc)
     */
    @GetMapping("/my-history")
    public ResponseEntity<ApiResponse<Page<WithdrawalResponse>>> getMyWithdrawalHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Long userId = userService.getCurrentUser().getId();
        
        // Create sort object
        Sort sort = sortDirection.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<WithdrawalResponse> response = withdrawalService.getWithdrawalHistoryPaged(userId, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(
                response,
                "Withdrawal history retrieved successfully"
        ));
    }
}
