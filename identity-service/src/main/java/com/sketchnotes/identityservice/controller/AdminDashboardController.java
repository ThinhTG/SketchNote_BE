package com.sketchnotes.identityservice.controller;

import com.sketchnotes.identityservice.dtos.ApiResponse;
import com.sketchnotes.identityservice.dtos.response.UserResponse;
import com.sketchnotes.identityservice.dtos.response.admin.AdminCreditTransactionResponse;
import com.sketchnotes.identityservice.dtos.response.admin.AdminTransactionResponse;
import com.sketchnotes.identityservice.dtos.response.admin.AdminUserSubscriptionResponse;
import com.sketchnotes.identityservice.dtos.response.admin.AdminWalletResponse;
import com.sketchnotes.identityservice.enums.CreditTransactionType;
import com.sketchnotes.identityservice.enums.SubscriptionStatus;
import com.sketchnotes.identityservice.enums.TransactionType;
import com.sketchnotes.identityservice.service.interfaces.IAdminDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller cho Admin Dashboard - Quản lý toàn bộ hệ thống
 * Base path: /api/admin/dashboard
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardController {
    
    private final IAdminDashboardService adminDashboardService;
    
    // ==================== USER MANAGEMENT ====================
    
    /**
     * Lấy danh sách tất cả users với phân trang và tìm kiếm
     * GET /api/admin/dashboard/users?search=&role=&page=0&size=10&sortBy=createdAt&sortDir=desc
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Admin: Getting all users - search='{}', role='{}', page={}, size={}", search, role, page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<UserResponse> users = adminDashboardService.getAllUsers(search, role, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }
    
    // ==================== WALLET MANAGEMENT ====================
    
    /**
     * Lấy danh sách tất cả wallets với phân trang và tìm kiếm
     * GET /api/admin/dashboard/wallets?search=&page=0&size=10
     */
    @GetMapping("/wallets")
    public ResponseEntity<ApiResponse<Page<AdminWalletResponse>>> getAllWallets(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Admin: Getting all wallets - search='{}', page={}, size={}", search, page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<AdminWalletResponse> wallets = adminDashboardService.getAllWallets(search, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(wallets, "Wallets retrieved successfully"));
    }
    
    /**
     * Lấy wallet của một user cụ thể
     * GET /api/admin/dashboard/wallets/user/{userId}
     */
    @GetMapping("/wallets/user/{userId}")
    public ResponseEntity<ApiResponse<AdminWalletResponse>> getWalletByUserId(@PathVariable Long userId) {
        log.info("Admin: Getting wallet for user {}", userId);
        
        AdminWalletResponse wallet = adminDashboardService.getWalletByUserId(userId);
        
        return ResponseEntity.ok(ApiResponse.success(wallet, "Wallet retrieved successfully"));
    }
    
    // ==================== TRANSACTION MANAGEMENT ====================
    
    /**
     * Lấy danh sách tất cả transactions (wallet) với phân trang và filter
     * GET /api/admin/dashboard/transactions?search=&type=&page=0&size=10
     */
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<Page<AdminTransactionResponse>>> getAllTransactions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Admin: Getting all transactions - search='{}', type='{}', page={}, size={}", search, type, page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<AdminTransactionResponse> transactions = adminDashboardService.getAllTransactions(search, type, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(transactions, "Transactions retrieved successfully"));
    }
    
    /**
     * Lấy transactions của một user cụ thể
     * GET /api/admin/dashboard/transactions/user/{userId}?page=0&size=10
     */
    @GetMapping("/transactions/user/{userId}")
    public ResponseEntity<ApiResponse<Page<AdminTransactionResponse>>> getTransactionsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Admin: Getting transactions for user {}", userId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AdminTransactionResponse> transactions = adminDashboardService.getTransactionsByUserId(userId, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(transactions, "User transactions retrieved successfully"));
    }
    
    // ==================== CREDIT TRANSACTION MANAGEMENT ====================
    
    /**
     * Lấy danh sách tất cả credit transactions với phân trang và filter
     * GET /api/admin/dashboard/credit-transactions?search=&type=&page=0&size=10
     * 
     * Type options: PURCHASE, PACKAGE_PURCHASE, USAGE, REFUND, BONUS, INITIAL_BONUS
     */
    @GetMapping("/credit-transactions")
    public ResponseEntity<ApiResponse<Page<AdminCreditTransactionResponse>>> getAllCreditTransactions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CreditTransactionType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Admin: Getting all credit transactions - search='{}', type='{}', page={}, size={}", search, type, page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<AdminCreditTransactionResponse> transactions = adminDashboardService.getAllCreditTransactions(search, type, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(transactions, "Credit transactions retrieved successfully"));
    }
    
    /**
     * Lấy credit transactions của một user cụ thể
     * GET /api/admin/dashboard/credit-transactions/user/{userId}?page=0&size=10
     */
    @GetMapping("/credit-transactions/user/{userId}")
    public ResponseEntity<ApiResponse<Page<AdminCreditTransactionResponse>>> getCreditTransactionsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Admin: Getting credit transactions for user {}", userId);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminCreditTransactionResponse> transactions = adminDashboardService.getCreditTransactionsByUserId(userId, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(transactions, "User credit transactions retrieved successfully"));
    }
    
    // ==================== SUBSCRIPTION MANAGEMENT ====================
    
    /**
     * Lấy danh sách tất cả user subscriptions với phân trang và filter
     * GET /api/admin/dashboard/subscriptions?search=&status=&planId=&page=0&size=10
     * 
     * Status options: ACTIVE, EXPIRED, CANCELLED
     */
    @GetMapping("/subscriptions")
    public ResponseEntity<ApiResponse<Page<AdminUserSubscriptionResponse>>> getAllUserSubscriptions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) SubscriptionStatus status,
            @RequestParam(required = false) Long planId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Admin: Getting all subscriptions - search='{}', status='{}', planId='{}', page={}, size={}", 
                search, status, planId, page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<AdminUserSubscriptionResponse> subscriptions = adminDashboardService.getAllUserSubscriptions(search, status, planId, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(subscriptions, "Subscriptions retrieved successfully"));
    }
    
    /**
     * Lấy subscriptions của một user cụ thể
     * GET /api/admin/dashboard/subscriptions/user/{userId}?page=0&size=10
     */
    @GetMapping("/subscriptions/user/{userId}")
    public ResponseEntity<ApiResponse<Page<AdminUserSubscriptionResponse>>> getSubscriptionsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Admin: Getting subscriptions for user {}", userId);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminUserSubscriptionResponse> subscriptions = adminDashboardService.getSubscriptionsByUserId(userId, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(subscriptions, "User subscriptions retrieved successfully"));
    }
}
