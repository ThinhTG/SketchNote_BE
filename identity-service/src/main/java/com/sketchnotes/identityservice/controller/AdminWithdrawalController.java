package com.sketchnotes.identityservice.controller;

import com.sketchnotes.identityservice.dtos.ApiResponse;
import com.sketchnotes.identityservice.dtos.request.ApproveRequest;
import com.sketchnotes.identityservice.dtos.request.RejectWithdrawalRequest;
import com.sketchnotes.identityservice.dtos.response.WithdrawalResponse;
import com.sketchnotes.identityservice.enums.WithdrawalStatus;
import com.sketchnotes.identityservice.service.interfaces.IUserService;
import com.sketchnotes.identityservice.service.interfaces.IWithdrawalService;
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
    public ResponseEntity<ApiResponse<WithdrawalResponse>> approveWithdrawal(@PathVariable Long id, @RequestBody ApproveRequest dto) {
        Long staffId = userService.getCurrentUser().getId();
        WithdrawalResponse response = withdrawalService.approveWithdrawal(id, staffId, dto);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Withdrawal approved and marked as completed."));
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
     * Get all withdrawal requests with pagination and search.
     * GET /api/admin/withdraw/all?search=&status=&page=0&size=10&sort=createdAt,desc
     * 
     * @param search Search keyword (optional) - searches in bank name, account number, account holder
     * @param status Filter by status (optional) - PENDING, APPROVED, REJECTED
     * @param page Page number (default: 0)
     * @param size Page size (default: 10)
     * @param sortBy Sort field (default: createdAt)
     * @param sortDirection Sort direction (default: desc)
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Page<WithdrawalResponse>>> getAllWithdrawals(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) WithdrawalStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        log.info("Getting all withdrawal requests - search: {}, status: {}, page: {}, size: {}", 
                search, status, page, size);
        
        // Create sort object
        Sort sort = sortDirection.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<WithdrawalResponse> response = withdrawalService.getAllWithdrawals(search, status, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(
                response,
                "All withdrawal requests retrieved successfully"
        ));
    }
}
