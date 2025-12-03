package com.sketchnotes.identityservice.service.implement;

import com.sketchnotes.identityservice.dtos.response.UserResponse;
import com.sketchnotes.identityservice.dtos.response.admin.AdminCreditTransactionResponse;
import com.sketchnotes.identityservice.dtos.response.admin.AdminTransactionResponse;
import com.sketchnotes.identityservice.dtos.response.admin.AdminUserSubscriptionResponse;
import com.sketchnotes.identityservice.dtos.response.admin.AdminWalletResponse;
import com.sketchnotes.identityservice.enums.CreditTransactionType;
import com.sketchnotes.identityservice.enums.SubscriptionStatus;
import com.sketchnotes.identityservice.enums.TransactionType;
import com.sketchnotes.identityservice.exception.AppException;
import com.sketchnotes.identityservice.exception.ErrorCode;
import com.sketchnotes.identityservice.model.*;
import com.sketchnotes.identityservice.repository.*;
import com.sketchnotes.identityservice.service.interfaces.IAdminDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation cho Admin Dashboard trong identity-service
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements IAdminDashboardService {
    
    private final IUserRepository userRepository;
    private final IWalletRepository walletRepository;
    private final ITransactionRepository transactionRepository;
    private final CreditTransactionRepository creditTransactionRepository;
    private final IUserSubscriptionRepository userSubscriptionRepository;
    
    // ==================== USER MANAGEMENT ====================
    
    @Override
    public Page<UserResponse> getAllUsers(String search, String role, Pageable pageable) {
        log.info("Admin: Getting all users with search='{}', role='{}'", search, role);
        
        Page<User> users;
        
        if (search != null && !search.trim().isEmpty()) {
            // Tìm kiếm theo email hoặc firstName hoặc lastName
            users = userRepository.findByEmailContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                    search.trim(), search.trim(), search.trim(), pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        
        // Filter by role if specified
        if (role != null && !role.trim().isEmpty()) {
            List<User> filteredUsers = users.getContent().stream()
                    .filter(user -> user.getRole() != null && 
                            user.getRole().name().equalsIgnoreCase(role))
                    .collect(Collectors.toList());
            return new PageImpl<>(filteredUsers.stream().map(this::mapToUserResponse).collect(Collectors.toList()), 
                    pageable, filteredUsers.size());
        }
        
        return users.map(this::mapToUserResponse);
    }    // ==================== WALLET MANAGEMENT ====================
    
    @Override
    public Page<AdminWalletResponse> getAllWallets(String search, Pageable pageable) {
        log.info("Admin: Getting all wallets with search='{}'", search);
        
        Page<Wallet> wallets = walletRepository.findAll(pageable);
        
        // If search is provided, filter by user email or name
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.trim().toLowerCase();
            List<AdminWalletResponse> filteredWallets = wallets.getContent().stream()
                    .filter(wallet -> {
                        User user = wallet.getUser();
                        return (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchLower)) ||
                               (user.getFirstName() != null && user.getFirstName().toLowerCase().contains(searchLower)) ||
                               (user.getLastName() != null && user.getLastName().toLowerCase().contains(searchLower));
                    })
                    .map(this::mapToAdminWalletResponse)
                    .collect(Collectors.toList());
            return new PageImpl<>(filteredWallets, pageable, filteredWallets.size());
        }
        
        return wallets.map(this::mapToAdminWalletResponse);
    }
    
    @Override
    public AdminWalletResponse getWalletByUserId(Long userId) {
        log.info("Admin: Getting wallet for user {}", userId);
        
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
        
        return mapToAdminWalletResponse(wallet);
    }
    
    // ==================== TRANSACTION MANAGEMENT ====================
    
    @Override
    public Page<AdminTransactionResponse> getAllTransactions(String search, TransactionType type, Pageable pageable) {
        log.info("Admin: Getting all transactions with search='{}', type='{}'", search, type);
        
        Page<Transaction> transactions = transactionRepository.findAll(pageable);
        
        List<AdminTransactionResponse> filteredTransactions = transactions.getContent().stream()
                .filter(tx -> {
                    // Filter by type if specified
                    if (type != null && tx.getType() != type) {
                        return false;
                    }
                    // Filter by search (user email, description, externalTransactionId)
                    if (search != null && !search.trim().isEmpty()) {
                        String searchLower = search.trim().toLowerCase();
                        User user = tx.getWallet().getUser();
                        return (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchLower)) ||
                               (tx.getDescription() != null && tx.getDescription().toLowerCase().contains(searchLower)) ||
                               (tx.getExternalTransactionId() != null && tx.getExternalTransactionId().toLowerCase().contains(searchLower));
                    }
                    return true;
                })
                .map(this::mapToAdminTransactionResponse)
                .collect(Collectors.toList());
        
        return new PageImpl<>(filteredTransactions, pageable, filteredTransactions.size());
    }
    
    @Override
    public Page<AdminTransactionResponse> getTransactionsByUserId(Long userId, Pageable pageable) {
        log.info("Admin: Getting transactions for user {}", userId);
        
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
        
        List<Transaction> transactions = transactionRepository.getTransactionByWalletWalletId(wallet.getWalletId());
        
        // Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), transactions.size());
        List<AdminTransactionResponse> pagedList = transactions.subList(start, end).stream()
                .map(this::mapToAdminTransactionResponse)
                .collect(Collectors.toList());
        
        return new PageImpl<>(pagedList, pageable, transactions.size());
    }
    
    // ==================== CREDIT TRANSACTION MANAGEMENT ====================
    
    @Override
    public Page<AdminCreditTransactionResponse> getAllCreditTransactions(String search, CreditTransactionType type, Pageable pageable) {
        log.info("Admin: Getting all credit transactions with search='{}', type='{}'", search, type);
        
        Page<CreditTransaction> transactions = creditTransactionRepository.findAll(pageable);
        
        List<AdminCreditTransactionResponse> filteredTransactions = transactions.getContent().stream()
                .filter(tx -> {
                    // Filter by type if specified
                    if (type != null && tx.getType() != type) {
                        return false;
                    }
                    // Filter by search (user email, description, referenceId)
                    if (search != null && !search.trim().isEmpty()) {
                        String searchLower = search.trim().toLowerCase();
                        User user = tx.getUser();
                        return (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchLower)) ||
                               (tx.getDescription() != null && tx.getDescription().toLowerCase().contains(searchLower)) ||
                               (tx.getReferenceId() != null && tx.getReferenceId().toLowerCase().contains(searchLower));
                    }
                    return true;
                })
                .map(this::mapToAdminCreditTransactionResponse)
                .collect(Collectors.toList());
        
        return new PageImpl<>(filteredTransactions, pageable, filteredTransactions.size());
    }
    
    @Override
    public Page<AdminCreditTransactionResponse> getCreditTransactionsByUserId(Long userId, Pageable pageable) {
        log.info("Admin: Getting credit transactions for user {}", userId);
        
        Page<CreditTransaction> transactions = creditTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        return transactions.map(this::mapToAdminCreditTransactionResponse);
    }
    
    // ==================== SUBSCRIPTION MANAGEMENT ====================
    
    @Override
    public Page<AdminUserSubscriptionResponse> getAllUserSubscriptions(String search, SubscriptionStatus status, Long planId, Pageable pageable) {
        log.info("Admin: Getting all subscriptions with search='{}', status='{}', planId='{}'", search, status, planId);
        
        Page<UserSubscription> subscriptions = userSubscriptionRepository.findAll(pageable);
        
        List<AdminUserSubscriptionResponse> filteredSubscriptions = subscriptions.getContent().stream()
                .filter(sub -> {
                    // Filter by status if specified
                    if (status != null && sub.getStatus() != status) {
                        return false;
                    }
                    // Filter by planId if specified
                    if (planId != null && !sub.getSubscriptionPlan().getPlanId().equals(planId)) {
                        return false;
                    }
                    // Filter by search (user email, user name, plan name)
                    if (search != null && !search.trim().isEmpty()) {
                        String searchLower = search.trim().toLowerCase();
                        User user = sub.getUser();
                        SubscriptionPlan plan = sub.getSubscriptionPlan();
                        return (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchLower)) ||
                               (user.getFirstName() != null && user.getFirstName().toLowerCase().contains(searchLower)) ||
                               (user.getLastName() != null && user.getLastName().toLowerCase().contains(searchLower)) ||
                               (plan.getPlanName() != null && plan.getPlanName().toLowerCase().contains(searchLower));
                    }
                    return true;
                })
                .map(this::mapToAdminUserSubscriptionResponse)
                .collect(Collectors.toList());
        
        return new PageImpl<>(filteredSubscriptions, pageable, filteredSubscriptions.size());
    }
    
    @Override
    public Page<AdminUserSubscriptionResponse> getSubscriptionsByUserId(Long userId, Pageable pageable) {
        log.info("Admin: Getting subscriptions for user {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        List<UserSubscription> subscriptions = userSubscriptionRepository.findByUser(user);
        
        // Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), subscriptions.size());
        List<AdminUserSubscriptionResponse> pagedList = subscriptions.subList(start, end).stream()
                .map(this::mapToAdminUserSubscriptionResponse)
                .collect(Collectors.toList());
        
        return new PageImpl<>(pagedList, pageable, subscriptions.size());
    }
    
    // ==================== MAPPING METHODS ====================
    
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .keycloakId(user.getKeycloakId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
    
    private AdminWalletResponse mapToAdminWalletResponse(Wallet wallet) {
        User user = wallet.getUser();
        return AdminWalletResponse.builder()
                .walletId(wallet.getWalletId())
                .userId(user.getId())
                .userEmail(user.getEmail())
                .userName(user.getFirstName() + " " + user.getLastName())
                .balance(wallet.getBalance())
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }
    
    private AdminTransactionResponse mapToAdminTransactionResponse(Transaction tx) {
        User user = tx.getWallet().getUser();
        return AdminTransactionResponse.builder()
                .transactionId(tx.getTransactionId())
                .walletId(tx.getWallet().getWalletId())
                .userId(user.getId())
                .userEmail(user.getEmail())
                .orderId(tx.getOrderId())
                .amount(tx.getAmount())
                .balanceAfter(tx.getBalance())
                .type(tx.getType())
                .status(tx.getStatus())
                .provider(tx.getProvider())
                .externalTransactionId(tx.getExternalTransactionId())
                .description(tx.getDescription())
                .orderCode(tx.getOrderCode())
                .createdAt(tx.getCreatedAt())
                .build();
    }
    
    private AdminCreditTransactionResponse mapToAdminCreditTransactionResponse(CreditTransaction tx) {
        User user = tx.getUser();
        return AdminCreditTransactionResponse.builder()
                .id(tx.getId())
                .userId(user.getId())
                .userEmail(user.getEmail())
                .userName(user.getFirstName() + " " + user.getLastName())
                .type(tx.getType())
                .amount(tx.getAmount())
                .balanceAfter(tx.getBalanceAfter())
                .description(tx.getDescription())
                .referenceId(tx.getReferenceId())
                .createdAt(tx.getCreatedAt())
                .build();
    }
    
    private AdminUserSubscriptionResponse mapToAdminUserSubscriptionResponse(UserSubscription sub) {
        User user = sub.getUser();
        SubscriptionPlan plan = sub.getSubscriptionPlan();
        return AdminUserSubscriptionResponse.builder()
                .subscriptionId(sub.getSubscriptionId())
                .userId(user.getId())
                .userEmail(user.getEmail())
                .userName(user.getFirstName() + " " + user.getLastName())
                .planId(plan.getPlanId())
                .planName(plan.getPlanName())
                .planType(plan.getPlanType() != null ? plan.getPlanType().name() : null)
                .price(plan.getPrice())
                .currency(plan.getCurrency())
                .status(sub.getStatus())
                .startDate(sub.getStartDate())
                .endDate(sub.getEndDate())
                .autoRenew(sub.getAutoRenew())
                .createdAt(sub.getCreatedAt())
                .build();
    }
}
