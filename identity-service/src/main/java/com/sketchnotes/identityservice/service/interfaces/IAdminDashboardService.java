package com.sketchnotes.identityservice.service.interfaces;

import com.sketchnotes.identityservice.dtos.response.UserResponse;
import com.sketchnotes.identityservice.dtos.response.admin.AdminCreditTransactionResponse;
import com.sketchnotes.identityservice.dtos.response.admin.AdminTransactionResponse;
import com.sketchnotes.identityservice.dtos.response.admin.AdminUserSubscriptionResponse;
import com.sketchnotes.identityservice.dtos.response.admin.AdminWalletResponse;
import com.sketchnotes.identityservice.enums.CreditTransactionType;
import com.sketchnotes.identityservice.enums.SubscriptionStatus;
import com.sketchnotes.identityservice.enums.TransactionType;
import com.sketchnotes.identityservice.ultils.PagedResponse;

/**
 * Service interface cho Admin Dashboard trong identity-service
 */
public interface IAdminDashboardService {
    
    // ==================== USER MANAGEMENT ====================
    
    /**
     * Lấy danh sách tất cả users với phân trang và tìm kiếm
     */
    PagedResponse<UserResponse> getAllUsers(String search, String role, int pageNo, int pageSize, String sortBy, String sortDir);
    
    // ==================== WALLET MANAGEMENT ====================
    
    /**
     * Lấy danh sách tất cả wallets với phân trang và tìm kiếm
     */
    PagedResponse<AdminWalletResponse> getAllWallets(String search, int pageNo, int pageSize, String sortBy, String sortDir);
    
    /**
     * Lấy wallet theo userId
     */
    AdminWalletResponse getWalletByUserId(Long userId);
    
    // ==================== TRANSACTION MANAGEMENT ====================
    
    /**
     * Lấy danh sách tất cả transactions (wallet) với phân trang và filter
     */
    PagedResponse<AdminTransactionResponse> getAllTransactions(
            String search, 
            TransactionType type,
            int pageNo, 
            int pageSize,
            String sortBy,
            String sortDir
    );
    
    /**
     * Lấy transactions của một user cụ thể
     */
    PagedResponse<AdminTransactionResponse> getTransactionsByUserId(Long userId, int pageNo, int pageSize, String sortBy, String sortDir);
    
    // ==================== CREDIT TRANSACTION MANAGEMENT ====================
    
    /**
     * Lấy danh sách tất cả credit transactions với phân trang và filter
     */
    PagedResponse<AdminCreditTransactionResponse> getAllCreditTransactions(
            String search, 
            CreditTransactionType type,
            int pageNo, 
            int pageSize,
            String sortBy,
            String sortDir
    );
    
    /**
     * Lấy credit transactions của một user cụ thể
     */
    PagedResponse<AdminCreditTransactionResponse> getCreditTransactionsByUserId(Long userId, int pageNo, int pageSize, String sortBy, String sortDir);
    
    // ==================== SUBSCRIPTION MANAGEMENT ====================
    
    /**
     * Lấy danh sách tất cả user subscriptions với phân trang và filter
     */
    PagedResponse<AdminUserSubscriptionResponse> getAllUserSubscriptions(
            String search, 
            SubscriptionStatus status,
            Long planId,
            int pageNo, 
            int pageSize,
            String sortBy,
            String sortDir
    );
    
    /**
     * Lấy subscriptions của một user cụ thể
     */
    PagedResponse<AdminUserSubscriptionResponse> getSubscriptionsByUserId(Long userId, int pageNo, int pageSize, String sortBy, String sortDir);
}
