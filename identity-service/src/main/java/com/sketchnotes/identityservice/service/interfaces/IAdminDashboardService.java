package com.sketchnotes.identityservice.service.interfaces;

import com.sketchnotes.identityservice.dtos.response.UserResponse;
import com.sketchnotes.identityservice.dtos.response.admin.AdminCreditTransactionResponse;
import com.sketchnotes.identityservice.dtos.response.admin.AdminTransactionResponse;
import com.sketchnotes.identityservice.dtos.response.admin.AdminUserSubscriptionResponse;
import com.sketchnotes.identityservice.dtos.response.admin.AdminWalletResponse;
import com.sketchnotes.identityservice.enums.CreditTransactionType;
import com.sketchnotes.identityservice.enums.SubscriptionStatus;
import com.sketchnotes.identityservice.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface cho Admin Dashboard trong identity-service
 */
public interface IAdminDashboardService {
    
    // ==================== USER MANAGEMENT ====================
    
    /**
     * Lấy danh sách tất cả users với phân trang và tìm kiếm
     */
    Page<UserResponse> getAllUsers(String search, String role, Pageable pageable);
    
    // ==================== WALLET MANAGEMENT ====================
    
    /**
     * Lấy danh sách tất cả wallets với phân trang và tìm kiếm
     */
    Page<AdminWalletResponse> getAllWallets(String search, Pageable pageable);
    
    /**
     * Lấy wallet theo userId
     */
    AdminWalletResponse getWalletByUserId(Long userId);
    
    // ==================== TRANSACTION MANAGEMENT ====================
    
    /**
     * Lấy danh sách tất cả transactions (wallet) với phân trang và filter
     */
    Page<AdminTransactionResponse> getAllTransactions(
            String search, 
            TransactionType type,
            Pageable pageable
    );
    
    /**
     * Lấy transactions của một user cụ thể
     */
    Page<AdminTransactionResponse> getTransactionsByUserId(Long userId, Pageable pageable);
    
    // ==================== CREDIT TRANSACTION MANAGEMENT ====================
    
    /**
     * Lấy danh sách tất cả credit transactions với phân trang và filter
     */
    Page<AdminCreditTransactionResponse> getAllCreditTransactions(
            String search, 
            CreditTransactionType type,
            Pageable pageable
    );
    
    /**
     * Lấy credit transactions của một user cụ thể
     */
    Page<AdminCreditTransactionResponse> getCreditTransactionsByUserId(Long userId, Pageable pageable);
    
    // ==================== SUBSCRIPTION MANAGEMENT ====================
    
    /**
     * Lấy danh sách tất cả user subscriptions với phân trang và filter
     */
    Page<AdminUserSubscriptionResponse> getAllUserSubscriptions(
            String search, 
            SubscriptionStatus status,
            Long planId,
            Pageable pageable
    );
    
    /**
     * Lấy subscriptions của một user cụ thể
     */
    Page<AdminUserSubscriptionResponse> getSubscriptionsByUserId(Long userId, Pageable pageable);
}
