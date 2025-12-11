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
import com.sketchnotes.identityservice.ultils.PagedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Admin: Getting all users - search='{}', role='{}', page={}, size={}, sortBy={}, sortDir={}", 
                search, role, page, size, sortBy, sortDir);
        
        PagedResponse<UserResponse> users = adminDashboardService.getAllUsers(search, role, page, size, sortBy, sortDir);
        
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }
    
    // ==================== WALLET MANAGEMENT ====================
    
    /**
     * Lấy danh sách tất cả wallets với phân trang và tìm kiếm
     * GET /api/admin/dashboard/wallets?search=&page=0&size=10&sortBy=createdAt&sortDir=desc
     */
    @GetMapping("/wallets")
    public ResponseEntity<ApiResponse<PagedResponse<AdminWalletResponse>>> getAllWallets(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Admin: Getting all wallets - search='{}', page={}, size={}, sortBy={}, sortDir={}", 
                search, page, size, sortBy, sortDir);
        
        PagedResponse<AdminWalletResponse> wallets = adminDashboardService.getAllWallets(search, page, size, sortBy, sortDir);
        
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
     * GET /api/admin/dashboard/transactions?search=&type=&page=0&size=10&sortBy=createdAt&sortDir=desc
     */
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<PagedResponse<AdminTransactionResponse>>> getAllTransactions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Admin: Getting all transactions - search='{}', type='{}', page={}, size={}, sortBy={}, sortDir={}", 
                search, type, page, size, sortBy, sortDir);
        
        PagedResponse<AdminTransactionResponse> transactions = adminDashboardService.getAllTransactions(search, type, page, size, sortBy, sortDir);
        
        return ResponseEntity.ok(ApiResponse.success(transactions, "Transactions retrieved successfully"));
    }
    
    /**
     * Lấy transactions của một user cụ thể
     * GET /api/admin/dashboard/transactions/user/{userId}?page=0&size=10&sortBy=createdAt&sortDir=desc
     */
    @GetMapping("/transactions/user/{userId}")
    public ResponseEntity<ApiResponse<PagedResponse<AdminTransactionResponse>>> getTransactionsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Admin: Getting transactions for user {} - sortBy={}, sortDir={}", userId, sortBy, sortDir);
        
        PagedResponse<AdminTransactionResponse> transactions = adminDashboardService.getTransactionsByUserId(userId, page, size, sortBy, sortDir);
        
        return ResponseEntity.ok(ApiResponse.success(transactions, "User transactions retrieved successfully"));
    }
    
    // ==================== CREDIT TRANSACTION MANAGEMENT ====================
    
    /**
     * Lấy danh sách tất cả credit transactions với phân trang và filter
     * GET /api/admin/dashboard/credit-transactions?search=&type=&page=0&size=10&sortBy=createdAt&sortDir=desc
     * 
     * Type options: PURCHASE, PACKAGE_PURCHASE, USAGE, REFUND, BONUS, INITIAL_BONUS
     */
    @GetMapping("/credit-transactions")
    public ResponseEntity<ApiResponse<PagedResponse<AdminCreditTransactionResponse>>> getAllCreditTransactions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CreditTransactionType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Admin: Getting all credit transactions - search='{}', type='{}', page={}, size={}, sortBy={}, sortDir={}", 
                search, type, page, size, sortBy, sortDir);
        
        PagedResponse<AdminCreditTransactionResponse> transactions = adminDashboardService.getAllCreditTransactions(search, type, page, size, sortBy, sortDir);
        
        return ResponseEntity.ok(ApiResponse.success(transactions, "Credit transactions retrieved successfully"));
    }
    
    /**
     * Lấy credit transactions của một user cụ thể
     * GET /api/admin/dashboard/credit-transactions/user/{userId}?page=0&size=10&sortBy=createdAt&sortDir=desc
     */
    @GetMapping("/credit-transactions/user/{userId}")
    public ResponseEntity<ApiResponse<PagedResponse<AdminCreditTransactionResponse>>> getCreditTransactionsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Admin: Getting credit transactions for user {} - sortBy={}, sortDir={}", userId, sortBy, sortDir);
        
        PagedResponse<AdminCreditTransactionResponse> transactions = adminDashboardService.getCreditTransactionsByUserId(userId, page, size, sortBy, sortDir);
        
        return ResponseEntity.ok(ApiResponse.success(transactions, "User credit transactions retrieved successfully"));
    }
    
    // ==================== SUBSCRIPTION MANAGEMENT ====================
    
    /**
     * Lấy danh sách tất cả user subscriptions với phân trang và filter
     * GET /api/admin/dashboard/subscriptions?search=&status=&planId=&page=0&size=10&sortBy=startDate&sortDir=desc
     * 
     * Status options: ACTIVE, EXPIRED, CANCELLED
     */
    @GetMapping("/subscriptions")
    public ResponseEntity<ApiResponse<PagedResponse<AdminUserSubscriptionResponse>>> getAllUserSubscriptions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) SubscriptionStatus status,
            @RequestParam(required = false) Long planId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Admin: Getting all subscriptions - search='{}', status='{}', planId='{}', page={}, size={}, sortBy={}, sortDir={}", 
                search, status, planId, page, size, sortBy, sortDir);
        
        PagedResponse<AdminUserSubscriptionResponse> subscriptions = adminDashboardService.getAllUserSubscriptions(search, status, planId, page, size, sortBy, sortDir);
        
        return ResponseEntity.ok(ApiResponse.success(subscriptions, "Subscriptions retrieved successfully"));
    }
    
    /**
     * Lấy subscriptions của một user cụ thể
     * GET /api/admin/dashboard/subscriptions/user/{userId}?page=0&size=10&sortBy=startDate&sortDir=desc
     */
    @GetMapping("/subscriptions/user/{userId}")
    public ResponseEntity<ApiResponse<PagedResponse<AdminUserSubscriptionResponse>>> getSubscriptionsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Admin: Getting subscriptions for user {} - sortBy={}, sortDir={}", userId, sortBy, sortDir);
        
        PagedResponse<AdminUserSubscriptionResponse> subscriptions = adminDashboardService.getSubscriptionsByUserId(userId, page, size, sortBy, sortDir);
        
        return ResponseEntity.ok(ApiResponse.success(subscriptions, "User subscriptions retrieved successfully"));
    }
}
