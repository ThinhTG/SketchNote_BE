package com.sketchnotes.identityservice.service;

import com.sketchnotes.identityservice.dtos.request.PurchaseCreditRequest;
import com.sketchnotes.identityservice.dtos.request.UseCreditRequest;
import com.sketchnotes.identityservice.dtos.response.CreditBalanceResponse;
import com.sketchnotes.identityservice.dtos.response.CreditTransactionResponse;
import com.sketchnotes.identityservice.enums.CreditTransactionType;
import com.sketchnotes.identityservice.exception.AppException;
import com.sketchnotes.identityservice.exception.ErrorCode;
import com.sketchnotes.identityservice.model.CreditTransaction;
import com.sketchnotes.identityservice.model.User;
import com.sketchnotes.identityservice.repository.CreditTransactionRepository;
import com.sketchnotes.identityservice.repository.IUserRepository;
import com.sketchnotes.identityservice.service.interfaces.IWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Service implementation cho quản lý AI Credits
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreditService implements ICreditService {
    
    private final IUserRepository userRepository;
    private final CreditTransactionRepository creditTransactionRepository;
    private final IWalletService walletService;
    
    // Số credit miễn phí cho user mới
    private static final Integer INITIAL_FREE_CREDITS = 50;
    
    // Giá mỗi credit (VNĐ) - có thể config từ properties
    private static final Integer CREDIT_PRICE = 1000; // 1000 VNĐ / credit
    
    @Override
    @Transactional
    public CreditBalanceResponse purchaseCredits(Long userId, PurchaseCreditRequest request) {
        log.info("User {} purchasing {} credits", userId, request.getAmount());
        
        // Validate minimum purchase
        if (request.getAmount() < 100) {
            throw new AppException(ErrorCode.MINIMUM_PURCHASE_NOT_MET);
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        // Tính tổng tiền cần thanh toán
        BigDecimal totalAmount = BigDecimal.valueOf(request.getAmount()).multiply(BigDecimal.valueOf(CREDIT_PRICE));
        
                // Deduct from wallet and record specific purchase type
                try {
                        String desc = "Purchase AI credits: " + request.getAmount() + " credits";
                            walletService.payWithType(user.getWallet().getWalletId(), totalAmount, com.sketchnotes.identityservice.enums.TransactionType.PURCHASE_AI_CREDITS, desc);
                        log.info("User {} wallet deducted: {} VNĐ for AI credits", userId, totalAmount);
        } catch (RuntimeException e) {
            log.error("Failed to deduct from wallet for user {}: {}", userId, e.getMessage());
            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);
        }
        
        // Cập nhật số credit của user
        Integer oldBalance = user.getAiCredits();
        Integer newBalance = oldBalance + request.getAmount();
        user.setAiCredits(newBalance);
        userRepository.save(user);
        
        // Tạo transaction record cho credit
        CreditTransaction transaction = CreditTransaction.builder()
                .user(user)
                .type(CreditTransactionType.PURCHASE)
                .amount(request.getAmount())
                .balanceAfter(newBalance)
                .description("Purchased " + request.getAmount() + " credits for " + totalAmount + " VNĐ")
                .build();
        creditTransactionRepository.save(transaction);
        
        log.info("User {} successfully purchased {} credits. New balance: {}", 
                userId, request.getAmount(), newBalance);
        
        return buildCreditBalanceResponse(user);
    }
    
    @Override
    @Transactional
    public CreditBalanceResponse useCredits(UseCreditRequest request) {
        log.info("User {} using {} credits for: {}", 
                request.getUserId(), request.getAmount(), request.getDescription());
        
        if (request.getAmount() <= 0) {
            throw new AppException(ErrorCode.INVALID_CREDIT_AMOUNT);
        }
        
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        // Kiểm tra số dư
        if (user.getAiCredits() < request.getAmount()) {
            log.warn("User {} has insufficient credits. Required: {}, Available: {}", 
                    request.getUserId(), request.getAmount(), user.getAiCredits());
            throw new AppException(ErrorCode.INSUFFICIENT_CREDITS);
        }
        
        // Trừ credit
        Integer oldBalance = user.getAiCredits();
        Integer newBalance = oldBalance - request.getAmount();
        user.setAiCredits(newBalance);
        userRepository.save(user);
        
        // Tạo transaction record (amount âm để đánh dấu là sử dụng)
        CreditTransaction transaction = CreditTransaction.builder()
                .user(user)
                .type(CreditTransactionType.USAGE)
                .amount(-request.getAmount()) // Âm vì là sử dụng
                .balanceAfter(newBalance)
                .description(request.getDescription() != null ? 
                        request.getDescription() : "AI service usage")
                .referenceId(request.getReferenceId())
                .build();
        creditTransactionRepository.save(transaction);
        
        log.info("User {} successfully used {} credits. New balance: {}", 
                request.getUserId(), request.getAmount(), newBalance);
        
        return buildCreditBalanceResponse(user);
    }
    
    @Override
    public boolean hasEnoughCredits(Long userId, Integer requiredAmount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        return user.getAiCredits() >= requiredAmount;
    }
    
    @Override
    public CreditBalanceResponse getCreditBalance(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        return buildCreditBalanceResponse(user);
    }
    
    @Override
    public Page<CreditTransactionResponse> getCreditHistory(Long userId, Pageable pageable) {
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        
        Page<CreditTransaction> transactions = creditTransactionRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        return transactions.map(this::mapToResponse);
    }
    
    @Override
    @Transactional
    public void grantInitialCredits(Long userId, Integer amount) {
        log.info("Granting {} initial credits to user {}", amount, userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        // Kiểm tra xem user đã nhận initial credits chưa
        boolean hasReceivedInitialCredits = creditTransactionRepository
                .findByUserIdAndType(userId, CreditTransactionType.INITIAL_BONUS)
                .size() > 0;
        
        if (hasReceivedInitialCredits) {
            log.warn("User {} has already received initial credits", userId);
            return;
        }
        
        // Cập nhật số credit
        Integer newBalance = user.getAiCredits() + amount;
        user.setAiCredits(newBalance);
        userRepository.save(user);
        
        // Tạo transaction record
        CreditTransaction transaction = CreditTransaction.builder()
                .user(user)
                .type(CreditTransactionType.INITIAL_BONUS)
                .amount(amount)
                .balanceAfter(newBalance)
                .description("Welcome bonus: " + amount + " free credits")
                .build();
        creditTransactionRepository.save(transaction);
        
        log.info("Successfully granted {} initial credits to user {}. New balance: {}", 
                amount, userId, newBalance);
    }
    
    /**
     * Helper method để build CreditBalanceResponse
     */
    private CreditBalanceResponse buildCreditBalanceResponse(User user) {
        Integer totalPurchased = creditTransactionRepository.getTotalCreditsPurchased(user.getId());
        Integer totalUsed = creditTransactionRepository.getTotalCreditsUsed(user.getId());
        Long usageCount = creditTransactionRepository.countAiUsage(user.getId());
        
        return CreditBalanceResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .currentBalance(user.getAiCredits())
                .totalPurchased(totalPurchased != null ? totalPurchased : 0)
                .totalUsed(totalUsed != null ? totalUsed : 0)
                .usageCount(usageCount != null ? usageCount : 0L)
                .build();
    }
    
    /**
     * Helper method để map CreditTransaction sang Response DTO
     */
    private CreditTransactionResponse mapToResponse(CreditTransaction transaction) {
        return CreditTransactionResponse.builder()
                .id(transaction.getId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .balanceAfter(transaction.getBalanceAfter())
                .description(transaction.getDescription())
                .referenceId(transaction.getReferenceId())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
