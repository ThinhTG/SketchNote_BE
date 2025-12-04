package com.sketchnotes.identityservice.controller;

import com.sketchnotes.identityservice.dtos.ApiResponse;
import com.sketchnotes.identityservice.dtos.request.RejectWithdrawalRequest;
import com.sketchnotes.identityservice.dtos.response.WithdrawalResponse;
import com.sketchnotes.identityservice.service.interfaces.IUserService;
import com.sketchnotes.identityservice.service.interfaces.IWithdrawalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin controller for staff to manage withdrawal requests.
 */
@RestController
@RequestMapping("/api/admin/withdraw")
@RequiredArgsConstructor
@Slf4j
public class AdminWithdrawalController {
    
    private final IWithdrawalService withdrawalService;
    private final IUserService userService;
    
    /**
     * Staff approves a withdrawal request.
     * PUT /api/admin/withdraw/{id}/approve
     */
    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<WithdrawalResponse>> approveWithdrawal(@PathVariable Long id) {
        log.info("Approving withdrawal request: {}", id);
        
        Long staffId = userService.getCurrentUser().getId();
        WithdrawalResponse response = withdrawalService.approveWithdrawal(id, staffId);
        
        return ResponseEntity.ok(ApiResponse.success(
                response,
                "Withdrawal approved and marked as completed."
        ));
    }
    
    /**
     * Staff rejects a withdrawal request.
     * PUT /api/admin/withdraw/{id}/reject
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<WithdrawalResponse>> rejectWithdrawal(
            @PathVariable Long id,
            @RequestBody(required = false) RejectWithdrawalRequest request) {
        log.info("Rejecting withdrawal request: {}", id);
        
        Long staffId = userService.getCurrentUser().getId();
        
        if (request == null) {
            request = new RejectWithdrawalRequest();
        }
        
        WithdrawalResponse response = withdrawalService.rejectWithdrawal(id, staffId, request);
        
        return ResponseEntity.ok(ApiResponse.success(
                response,
                "Withdrawal request has been rejected."
        ));
    }
    
    /**
     * Get all pending withdrawal requests.
     * GET /api/admin/withdraw/pending
     */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<WithdrawalResponse>>> getPendingWithdrawals() {
        log.info("Getting all pending withdrawal requests");
        
        List<WithdrawalResponse> response = withdrawalService.getPendingWithdrawals();
        
        return ResponseEntity.ok(ApiResponse.success(
                response,
                "Pending withdrawal requests retrieved successfully"
        ));
    }
    
    /**
     * Get all withdrawal requests.
     * GET /api/admin/withdraw/all
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<WithdrawalResponse>>> getAllWithdrawals() {
        log.info("Getting all withdrawal requests");
        
        List<WithdrawalResponse> response = withdrawalService.getAllWithdrawals();
        
        return ResponseEntity.ok(ApiResponse.success(
                response,
                "All withdrawal requests retrieved successfully"
        ));
    }
}
