package com.sketchnotes.identityservice.service.implement;

import com.sketchnotes.identityservice.dtos.response.UserResponse;
import com.sketchnotes.identityservice.dtos.response.admin.AdminCreditTransactionResponse;
import com.sketchnotes.identityservice.dtos.response.admin.AdminTransactionResponse;
import com.sketchnotes.identityservice.dtos.response.admin.AdminUserSubscriptionResponse;
import com.sketchnotes.identityservice.dtos.response.admin.AdminWalletResponse;
import com.sketchnotes.identityservice.enums.CreditTransactionType;
import com.sketchnotes.identityservice.enums.Role;
import com.sketchnotes.identityservice.enums.SubscriptionStatus;
import com.sketchnotes.identityservice.enums.TransactionType;
import com.sketchnotes.identityservice.exception.AppException;
import com.sketchnotes.identityservice.exception.ErrorCode;
import com.sketchnotes.identityservice.model.*;
import com.sketchnotes.identityservice.repository.*;
import com.sketchnotes.identityservice.service.interfaces.IAdminDashboardService;
import com.sketchnotes.identityservice.ultils.PagedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public PagedResponse<UserResponse> getAllUsers(String search, String role, int pageNo, int pageSize, String sortBy, String sortDir) {
        log.info("Admin: Getting all users with search='{}', role='{}'", search, role);
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        
        Page<User> users;
        Role roleEnum = null;
        
        // Parse role nếu có
        if (role != null && !role.trim().isEmpty()) {
            try {
                roleEnum = Role.valueOf(role.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid role: {}", role);
            }
        }
        
        boolean hasSearch = search != null && !search.trim().isEmpty();
        String searchTerm = hasSearch ? search.trim() : null;
        
        if (roleEnum != null && hasSearch) {
            // Tìm kiếm theo cả role và search
            users = userRepository.findByRoleAndEmailContainingIgnoreCaseOrRoleAndFirstNameContainingIgnoreCaseOrRoleAndLastNameContainingIgnoreCase(
                    roleEnum, searchTerm, roleEnum, searchTerm, roleEnum, searchTerm, pageable);
        } else if (roleEnum != null) {
            // Chỉ filter theo role
            users = userRepository.findByRole(roleEnum, pageable);
        } else if (hasSearch) {
            // Chỉ tìm kiếm theo 
            users = userRepository.findByEmailContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                    searchTerm, searchTerm, searchTerm, pageable);
        } else {
            // Lấy tất cả
            users = userRepository.findAll(pageable);
        }
        
        List<UserResponse> userResponses = users.getContent().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
        
        return PagedResponse.<UserResponse>builder()
                .content(userResponses)
                .pageNo(users.getNumber())
                .pageSize(users.getSize())
                .totalElements(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .isLast(users.isLast())
                .build();
    }    // ==================== WALLET MANAGEMENT ====================
    
    @Override
    public PagedResponse<AdminWalletResponse> getAllWallets(String search, int pageNo, int pageSize, String sortBy, String sortDir) {
        log.info("Admin: Getting all wallets with search='{}'", search);
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        
        Page<Wallet> wallets;
        
        if (search != null && !search.trim().isEmpty()) {
            wallets = walletRepository.searchByUserEmailOrName(search.trim(), pageable);
        } else {
            wallets = walletRepository.findAll(pageable);
        }
        
        List<AdminWalletResponse> walletResponses = wallets.getContent().stream()
                .map(this::mapToAdminWalletResponse)
                .collect(Collectors.toList());
        
        return PagedResponse.<AdminWalletResponse>builder()
                .content(walletResponses)
                .pageNo(wallets.getNumber())
                .pageSize(wallets.getSize())
                .totalElements(wallets.getTotalElements())
                .totalPages(wallets.getTotalPages())
                .isLast(wallets.isLast())
                .build();
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
    public PagedResponse<AdminTransactionResponse> getAllTransactions(String search, TransactionType type, int pageNo, int pageSize, String sortBy, String sortDir) {
        log.info("Admin: Getting all transactions with search='{}', type='{}'", search, type);
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        
        Page<Transaction> transactions;
        boolean hasSearch = search != null && !search.trim().isEmpty();
        String searchTerm = hasSearch ? search.trim() : null;
        
        if (type != null && hasSearch) {
            transactions = transactionRepository.searchByKeywordAndType(searchTerm, type, pageable);
        } else if (type != null) {
            transactions = transactionRepository.findByType(type, pageable);
        } else if (hasSearch) {
            transactions = transactionRepository.searchByKeyword(searchTerm, pageable);
        } else {
            transactions = transactionRepository.findAll(pageable);
        }
        
        List<AdminTransactionResponse> transactionResponses = transactions.getContent().stream()
                .map(this::mapToAdminTransactionResponse)
                .collect(Collectors.toList());
        
        return PagedResponse.<AdminTransactionResponse>builder()
                .content(transactionResponses)
                .pageNo(transactions.getNumber())
                .pageSize(transactions.getSize())
                .totalElements(transactions.getTotalElements())
                .totalPages(transactions.getTotalPages())
                .isLast(transactions.isLast())
                .build();
    }
    
    @Override
    public PagedResponse<AdminTransactionResponse> getTransactionsByUserId(Long userId, int pageNo, int pageSize, String sortBy, String sortDir) {
        log.info("Admin: Getting transactions for user {}", userId);
        
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Transaction> transactions = transactionRepository.findByWalletWalletIdOrderByCreatedAtDesc(wallet.getWalletId(), pageable);
        
        List<AdminTransactionResponse> transactionResponses = transactions.getContent().stream()
                .map(this::mapToAdminTransactionResponse)
                .collect(Collectors.toList());
        
        return PagedResponse.<AdminTransactionResponse>builder()
                .content(transactionResponses)
                .pageNo(transactions.getNumber())
                .pageSize(transactions.getSize())
                .totalElements(transactions.getTotalElements())
                .totalPages(transactions.getTotalPages())
                .isLast(transactions.isLast())
                .build();
    }
    
    // ==================== CREDIT TRANSACTION MANAGEMENT ====================
    
    @Override
    public PagedResponse<AdminCreditTransactionResponse> getAllCreditTransactions(String search, CreditTransactionType type, int pageNo, int pageSize, String sortBy, String sortDir) {
        log.info("Admin: Getting all credit transactions with search='{}', type='{}'", search, type);
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        
        Page<CreditTransaction> transactions;
        boolean hasSearch = search != null && !search.trim().isEmpty();
        String searchTerm = hasSearch ? search.trim() : null;
        
        if (type != null && hasSearch) {
            transactions = creditTransactionRepository.searchByKeywordAndType(searchTerm, type, pageable);
        } else if (type != null) {
            transactions = creditTransactionRepository.findByType(type, pageable);
        } else if (hasSearch) {
            transactions = creditTransactionRepository.searchByKeyword(searchTerm, pageable);
        } else {
            transactions = creditTransactionRepository.findAll(pageable);
        }
        
        List<AdminCreditTransactionResponse> transactionResponses = transactions.getContent().stream()
                .map(this::mapToAdminCreditTransactionResponse)
                .collect(Collectors.toList());
        
        return PagedResponse.<AdminCreditTransactionResponse>builder()
                .content(transactionResponses)
                .pageNo(transactions.getNumber())
                .pageSize(transactions.getSize())
                .totalElements(transactions.getTotalElements())
                .totalPages(transactions.getTotalPages())
                .isLast(transactions.isLast())
                .build();
    }
    
    @Override
    public PagedResponse<AdminCreditTransactionResponse> getCreditTransactionsByUserId(Long userId, int pageNo, int pageSize, String sortBy, String sortDir) {
        log.info("Admin: Getting credit transactions for user {}", userId);
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<CreditTransaction> transactions = creditTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        List<AdminCreditTransactionResponse> transactionResponses = transactions.getContent().stream()
                .map(this::mapToAdminCreditTransactionResponse)
                .collect(Collectors.toList());
        
        return PagedResponse.<AdminCreditTransactionResponse>builder()
                .content(transactionResponses)
                .pageNo(transactions.getNumber())
                .pageSize(transactions.getSize())
                .totalElements(transactions.getTotalElements())
                .totalPages(transactions.getTotalPages())
                .isLast(transactions.isLast())
                .build();
    }
    
    // ==================== SUBSCRIPTION MANAGEMENT ====================
    
    @Override
    public PagedResponse<AdminUserSubscriptionResponse> getAllUserSubscriptions(String search, SubscriptionStatus status, Long planId, int pageNo, int pageSize, String sortBy, String sortDir) {
        log.info("Admin: Getting all subscriptions with search='{}', status='{}', planId='{}'", search, status, planId);
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        
        Page<UserSubscription> subscriptions;
        boolean hasSearch = search != null && !search.trim().isEmpty();
        String searchTerm = hasSearch ? search.trim() : null;
        
        // Determine which query to use based on filters
        if (hasSearch && status != null && planId != null) {
            subscriptions = userSubscriptionRepository.searchByKeywordAndStatusAndPlanId(searchTerm, status, planId, pageable);
        } else if (hasSearch && status != null) {
            subscriptions = userSubscriptionRepository.searchByKeywordAndStatus(searchTerm, status, pageable);
        } else if (hasSearch && planId != null) {
            subscriptions = userSubscriptionRepository.searchByKeywordAndPlanId(searchTerm, planId, pageable);
        } else if (status != null && planId != null) {
            subscriptions = userSubscriptionRepository.findByStatusAndPlanId(status, planId, pageable);
        } else if (hasSearch) {
            subscriptions = userSubscriptionRepository.searchByKeyword(searchTerm, pageable);
        } else if (status != null) {
            subscriptions = userSubscriptionRepository.findByStatus(status, pageable);
        } else if (planId != null) {
            subscriptions = userSubscriptionRepository.findByPlanId(planId, pageable);
        } else {
            subscriptions = userSubscriptionRepository.findAll(pageable);
        }
        
        List<AdminUserSubscriptionResponse> subscriptionResponses = subscriptions.getContent().stream()
                .map(this::mapToAdminUserSubscriptionResponse)
                .collect(Collectors.toList());
        
        return PagedResponse.<AdminUserSubscriptionResponse>builder()
                .content(subscriptionResponses)
                .pageNo(subscriptions.getNumber())
                .pageSize(subscriptions.getSize())
                .totalElements(subscriptions.getTotalElements())
                .totalPages(subscriptions.getTotalPages())
                .isLast(subscriptions.isLast())
                .build();
    }
    
    @Override
    public PagedResponse<AdminUserSubscriptionResponse> getSubscriptionsByUserId(Long userId, int pageNo, int pageSize, String sortBy, String sortDir) {
        log.info("Admin: Getting subscriptions for user {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<UserSubscription> subscriptions = userSubscriptionRepository.findByUser(user, pageable);
        
        List<AdminUserSubscriptionResponse> subscriptionResponses = subscriptions.getContent().stream()
                .map(this::mapToAdminUserSubscriptionResponse)
                .collect(Collectors.toList());
        
        return PagedResponse.<AdminUserSubscriptionResponse>builder()
                .content(subscriptionResponses)
                .pageNo(subscriptions.getNumber())
                .pageSize(subscriptions.getSize())
                .totalElements(subscriptions.getTotalElements())
                .totalPages(subscriptions.getTotalPages())
                .isLast(subscriptions.isLast())
                .build();
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
