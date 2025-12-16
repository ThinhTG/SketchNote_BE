package com.sketchnotes.identityservice.service.implement;

import com.sketchnotes.identityservice.dtos.request.CreateNotificationRequest;
import com.sketchnotes.identityservice.dtos.request.RejectWithdrawalRequest;
import com.sketchnotes.identityservice.dtos.request.WithdrawalRequestDto;
import com.sketchnotes.identityservice.dtos.response.WithdrawalResponse;
import com.sketchnotes.identityservice.enums.NotificationType;
import com.sketchnotes.identityservice.enums.WithdrawalStatus;
import com.sketchnotes.identityservice.exception.AppException;
import com.sketchnotes.identityservice.exception.ErrorCode;
import com.sketchnotes.identityservice.model.User;
import com.sketchnotes.identityservice.model.Wallet;
import com.sketchnotes.identityservice.model.WithdrawalRequest;
import com.sketchnotes.identityservice.repository.IUserRepository;
import com.sketchnotes.identityservice.repository.IWalletRepository;
import com.sketchnotes.identityservice.repository.WithdrawalRequestRepository;
import com.sketchnotes.identityservice.service.interfaces.INotificationService;
import com.sketchnotes.identityservice.service.interfaces.IWithdrawalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for withdrawal request management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WithdrawalService implements IWithdrawalService {
    
    private final WithdrawalRequestRepository withdrawalRequestRepository;
    private final IWalletRepository walletRepository;
    private final IUserRepository userRepository;
    private final INotificationService notificationService;
    
    @Override
    @Transactional
    public WithdrawalResponse createWithdrawalRequest(Long userId, WithdrawalRequestDto request) {
        log.info("User {} creating withdrawal request for amount: {}", userId, request.getAmount());
        
        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        // Validate amount
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorCode.INVALID_WITHDRAWAL_AMOUNT);
        }
        
        // Get user's wallet
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
        
        // Check sufficient balance
        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);
        }
        
        // Check for pending withdrawal (optional business rule)
        if (withdrawalRequestRepository.existsByUserIdAndStatus(userId, WithdrawalStatus.PENDING)) {
            throw new AppException(ErrorCode.PENDING_WITHDRAWAL_EXISTS);
        }
        
        // Deduct amount from wallet (lock the money)
        wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
        walletRepository.save(wallet);
        
        // Create withdrawal request
        WithdrawalRequest withdrawalRequest = WithdrawalRequest.builder()
                .userId(userId)
                .amount(request.getAmount())
                .bankName(request.getBankName())
                .bankAccountNumber(request.getBankAccountNumber())
                .bankAccountHolder(request.getBankAccountHolder())
                .status(WithdrawalStatus.PENDING)
                .build();
        
        withdrawalRequest = withdrawalRequestRepository.save(withdrawalRequest);
        log.info("Withdrawal request {} created successfully for user {}", withdrawalRequest.getId(), userId);
        
        // Send notification to customer
        try {
            CreateNotificationRequest notificationRequest = CreateNotificationRequest.builder()
                    .userId(userId)
                    .type(NotificationType.SYSTEM)
                    .title("Withdrawal Request Submitted")
                    .message("Your withdrawal request was successfully submitted. Your money will be returned to your bank account within 24 hours.")
                    .build();
            notificationService.create(notificationRequest);
        } catch (Exception e) {
            log.error("Failed to send notification for withdrawal request {}: {}", withdrawalRequest.getId(), e.getMessage());
        }
        
        return mapToResponse(withdrawalRequest);
    }
    
    @Override
    public List<WithdrawalResponse> getWithdrawalHistory(Long userId) {
        log.info("Getting withdrawal history for user {}", userId);
        
        List<WithdrawalRequest> requests = withdrawalRequestRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        return requests.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public Page<WithdrawalResponse> getWithdrawalHistoryPaged(Long userId, Pageable pageable) {
        log.info("Getting withdrawal history for user {} with pagination - page={}, size={}", 
                userId, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<WithdrawalRequest> requests = withdrawalRequestRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        return requests.map(this::mapToResponse);
    }
    
    @Override
    @Transactional
    public WithdrawalResponse approveWithdrawal(Long withdrawalId, Long staffId) {
        log.info("Staff {} approving withdrawal request {}", staffId, withdrawalId);
        
        WithdrawalRequest withdrawalRequest = withdrawalRequestRepository.findById(withdrawalId)
                .orElseThrow(() -> new AppException(ErrorCode.WITHDRAWAL_NOT_FOUND));
        
        // Check if already processed
        if (withdrawalRequest.getStatus() != WithdrawalStatus.PENDING) {
            throw new AppException(ErrorCode.WITHDRAWAL_ALREADY_PROCESSED);
        }
        
        // Update status
        withdrawalRequest.setStatus(WithdrawalStatus.APPROVED);
        withdrawalRequest.setStaffId(staffId);
        withdrawalRequest = withdrawalRequestRepository.save(withdrawalRequest);
        
        log.info("Withdrawal request {} approved by staff {}", withdrawalId, staffId);
        
        // Send notification to customer
        try {
            CreateNotificationRequest notificationRequest = CreateNotificationRequest.builder()
                    .userId(withdrawalRequest.getUserId())
                    .type(NotificationType.SYSTEM)
                    .title("Withdrawal Completed")
                    .message("Your withdrawal has been completed. Please check your bank account.")
                    .build();
            notificationService.create(notificationRequest);
        } catch (Exception e) {
            log.error("Failed to send notification for withdrawal approval {}: {}", withdrawalId, e.getMessage());
        }
        
        return mapToResponse(withdrawalRequest);
    }
    
    @Override
    @Transactional
    public WithdrawalResponse rejectWithdrawal(Long withdrawalId, Long staffId, RejectWithdrawalRequest request) {
        log.info("Staff {} rejecting withdrawal request {}", staffId, withdrawalId);
        
        WithdrawalRequest withdrawalRequest = withdrawalRequestRepository.findById(withdrawalId)
                .orElseThrow(() -> new AppException(ErrorCode.WITHDRAWAL_NOT_FOUND));
        
        // Check if already processed
        if (withdrawalRequest.getStatus() != WithdrawalStatus.PENDING) {
            throw new AppException(ErrorCode.WITHDRAWAL_ALREADY_PROCESSED);
        }
        
        // Refund the money to wallet
        Wallet wallet = walletRepository.findByUserId(withdrawalRequest.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));
        
        wallet.setBalance(wallet.getBalance().add(withdrawalRequest.getAmount()));
        walletRepository.save(wallet);
        
        // Update status
        withdrawalRequest.setStatus(WithdrawalStatus.REJECTED);
        withdrawalRequest.setStaffId(staffId);
        withdrawalRequest.setRejectionReason(request.getRejectionReason());
        withdrawalRequest = withdrawalRequestRepository.save(withdrawalRequest);
        
        log.info("Withdrawal request {} rejected by staff {}. Money refunded to wallet.", withdrawalId, staffId);
        
        // Send notification to customer
        try {
            CreateNotificationRequest notificationRequest = CreateNotificationRequest.builder()
                    .userId(withdrawalRequest.getUserId())
                    .type(NotificationType.SYSTEM)
                    .title("Withdrawal Request Rejected")
                    .message("Your withdrawal request has been rejected. Please contact support for more information.")
                    .build();
            notificationService.create(notificationRequest);
        } catch (Exception e) {
            log.error("Failed to send notification for withdrawal rejection {}: {}", withdrawalId, e.getMessage());
        }
        
        return mapToResponse(withdrawalRequest);
    }
    
    @Override
    public List<WithdrawalResponse> getPendingWithdrawals() {
        log.info("Getting all pending withdrawal requests");
        
        List<WithdrawalRequest> requests = withdrawalRequestRepository.findByStatusOrderByCreatedAtAsc(WithdrawalStatus.PENDING);
        
        return requests.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public Page<WithdrawalResponse> getAllWithdrawals(String search, WithdrawalStatus status, Pageable pageable) {
        log.info("Getting all withdrawal requests with search='{}', status='{}', page={}, size={}", 
                search, status, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<WithdrawalRequest> requests;
        
        // Case 1: Search with status filter
        if (search != null && !search.trim().isEmpty() && status != null) {
            requests = withdrawalRequestRepository.searchWithdrawalsByStatus(search.trim(), status, pageable);
        }
        // Case 2: Search only
        else if (search != null && !search.trim().isEmpty()) {
            requests = withdrawalRequestRepository.searchWithdrawals(search.trim(), pageable);
        }
        // Case 3: Status filter only
        else if (status != null) {
            requests = withdrawalRequestRepository.findByStatus(status, pageable);
        }
        // Case 4: No filter, return all
        else {
            requests = withdrawalRequestRepository.findAll(pageable);
        }
        
        return requests.map(this::mapToResponse);
    }
    
    /**
     * Map entity to response DTO.
     */
    private WithdrawalResponse mapToResponse(WithdrawalRequest request) {
        return WithdrawalResponse.builder()
                .id(request.getId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .bankName(request.getBankName())
                .bankAccountNumber(request.getBankAccountNumber())
                .bankAccountHolder(request.getBankAccountHolder())
                .status(request.getStatus())
                .staffId(request.getStaffId())
                .rejectionReason(request.getRejectionReason())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }
}
