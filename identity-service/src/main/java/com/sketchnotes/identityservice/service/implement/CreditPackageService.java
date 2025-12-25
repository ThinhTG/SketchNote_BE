package com.sketchnotes.identityservice.service.implement;

import com.sketchnotes.identityservice.dtos.request.CreditPackageRequest;
import com.sketchnotes.identityservice.dtos.response.CreditPackageResponse;
import com.sketchnotes.identityservice.dtos.response.PurchasePackageResponse;
import com.sketchnotes.identityservice.enums.CreditTransactionType;
import com.sketchnotes.identityservice.enums.Role;
import com.sketchnotes.identityservice.enums.TransactionType;
import com.sketchnotes.identityservice.exception.AppException;
import com.sketchnotes.identityservice.exception.ErrorCode;
import com.sketchnotes.identityservice.model.CreditPackage;
import com.sketchnotes.identityservice.model.CreditTransaction;
import com.sketchnotes.identityservice.model.User;
import com.sketchnotes.identityservice.model.Wallet;
import com.sketchnotes.identityservice.repository.CreditPackageRepository;
import com.sketchnotes.identityservice.repository.CreditTransactionRepository;
import com.sketchnotes.identityservice.repository.IUserRepository;
import com.sketchnotes.identityservice.service.interfaces.INotificationService;
import com.sketchnotes.identityservice.service.interfaces.IUserService;
import com.sketchnotes.identityservice.service.interfaces.IWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation cho quản lý Credit Packages
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreditPackageService implements INotificationService.ICreditPackageService {
    
    private final CreditPackageRepository creditPackageRepository;
    private final IUserRepository userRepository;
    private final IWalletService walletService;
    private final CreditTransactionRepository creditTransactionRepository;
    private final IUserService userService;
    
    @Override
    @Transactional
    public CreditPackageResponse createPackage(CreditPackageRequest request) {
        if (request.getDiscountedPrice().compareTo(request.getOriginalPrice()) > 0) {
            throw new AppException(ErrorCode.INVALID_PRICE);
        }

        BigDecimal discountPercent = calculateDiscountPercent(
                request.getOriginalPrice(), 
                request.getDiscountedPrice()
        );
        
        CreditPackage creditPackage = CreditPackage.builder()
                .name(request.getName())
                .description(request.getDescription())
                .creditAmount(request.getCreditAmount())
                .originalPrice(request.getOriginalPrice())
                .discountedPrice(request.getDiscountedPrice())
                .discountPercent(discountPercent)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .isPopular(request.getIsPopular() != null ? request.getIsPopular() : false)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .build();
        
        CreditPackage saved = creditPackageRepository.save(creditPackage);
        return mapToResponse(saved);
    }
    
    @Override
    @Transactional
    public CreditPackageResponse updatePackage(Long id, CreditPackageRequest request) {

        CreditPackage creditPackage = creditPackageRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CREDIT_PACKAGE_NOT_FOUND));
        
        // Validate giá giảm không được lớn hơn giá gốc
        if (request.getDiscountedPrice().compareTo(request.getOriginalPrice()) > 0) {
            throw new AppException(ErrorCode.INVALID_PRICE);
        }
        
        // Tính phần trăm giảm giá
        BigDecimal discountPercent = calculateDiscountPercent(
                request.getOriginalPrice(), 
                request.getDiscountedPrice()
        );
        
        creditPackage.setName(request.getName());
        creditPackage.setDescription(request.getDescription());
        creditPackage.setCreditAmount(request.getCreditAmount());
        creditPackage.setOriginalPrice(request.getOriginalPrice());
        creditPackage.setDiscountedPrice(request.getDiscountedPrice());
        creditPackage.setDiscountPercent(discountPercent);
        if (request.getIsActive() != null) {
            creditPackage.setIsActive(request.getIsActive());
        }
        if (request.getIsPopular() != null) {
            creditPackage.setIsPopular(request.getIsPopular());
        }
        if (request.getDisplayOrder() != null) {
            creditPackage.setDisplayOrder(request.getDisplayOrder());
        }
        
        CreditPackage updated = creditPackageRepository.save(creditPackage);
        
        return mapToResponse(updated);
    }
    
    @Override
    @Transactional
    public void deletePackage(Long id) {
        log.info("Deleting credit package ID: {}", id);
        
        CreditPackage creditPackage = creditPackageRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CREDIT_PACKAGE_NOT_FOUND));
        
        // Soft delete bằng cách set isActive = false
        creditPackage.setIsActive(false);
        creditPackageRepository.save(creditPackage);
        
        log.info("Credit package deleted (deactivated) successfully: {}", id);
    }
    
    @Override
    public CreditPackageResponse getPackageById(Long id) {
        CreditPackage creditPackage = creditPackageRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CREDIT_PACKAGE_NOT_FOUND));
        
        return mapToResponse(creditPackage);
    }
    
    @Override
    public List<CreditPackageResponse> getActivePackages() {
        List<CreditPackage> packages = creditPackageRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
        
        return packages.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<CreditPackageResponse> getAllPackages() {
        List<CreditPackage> packages = creditPackageRepository.findAllByOrderByDisplayOrderAsc();
        
        return packages.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public CreditPackageResponse togglePackageStatus(Long id) {
        log.info("Toggling status for credit package ID: {}", id);
        
        CreditPackage creditPackage = creditPackageRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CREDIT_PACKAGE_NOT_FOUND));
        
        creditPackage.setIsActive(!creditPackage.getIsActive());
        CreditPackage updated = creditPackageRepository.save(creditPackage);
        
        log.info("Credit package {} status toggled to: {}", id, updated.getIsActive());
        
        return mapToResponse(updated);
    }
    
    @Override
    @Transactional
    public PurchasePackageResponse purchasePackage(Long userId, Long packageId) {
        log.info("User {} attempting to purchase credit package {}", userId, packageId);
        
        // 1. Validate và lấy thông tin user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userId);
                    return new AppException(ErrorCode.USER_NOT_FOUND);
                });
        
        // 2. Validate và lấy thông tin package
        CreditPackage creditPackage = creditPackageRepository.findById(packageId)
                .orElseThrow(() -> {
                    log.error("Credit package not found: {}", packageId);
                    return new AppException(ErrorCode.CREDIT_PACKAGE_NOT_FOUND);
                });
        
        // 3. Kiểm tra package có đang active không
        if (!creditPackage.getIsActive()) {
            log.warn("Attempted to purchase inactive credit package: {}", packageId);
            throw new AppException(ErrorCode.CREDIT_PACKAGE_NOT_ACTIVE);
        }
        
        // 4. Kiểm tra user có wallet không
        if (user.getWallet() == null) {
            log.error("User {} does not have a wallet", userId);
            throw new AppException(ErrorCode.WALLET_NOT_FOUND);
        }
        
        // Lưu số dư cũ trước khi thực hiện giao dịch
        Integer previousBalance = user.getAiCredits();
        BigDecimal amountToPay = creditPackage.getDiscountedPrice();
        
        // 5. Thực hiện thanh toán từ wallet
        try {
            String paymentDescription = String.format(
                    "Purchase credit package: %s (%d credits)", 
                    creditPackage.getName(), 
                    creditPackage.getCreditAmount()
            );
            
            walletService.payWithType(
                    user.getWallet().getWalletId(), 
                    amountToPay, 
                    TransactionType.PURCHASE_AI_CREDITS, 
                    paymentDescription
            );
            
            log.info("Successfully deducted {} {} from user {} wallet for package {}", 
                    amountToPay, creditPackage.getCurrency(), userId, packageId);
                    
        } catch (AppException e) {
            log.error("Failed to process payment for user {}: {}", userId, e.getMessage());
            throw e; // Re-throw để giữ nguyên error code
        } catch (RuntimeException e) {
            log.error("Unexpected error during payment for user {}: {}", userId, e.getMessage());
            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);
        }
        // cộng tiền cho admin
        var Admin = userService.getUsersByRole(Role.ADMIN);
        if (Admin.isEmpty()) {
            log.error("No admin user found to receive course fee");
        } else {
            var adminUser = Admin.get(0); // Lấy admin đầu tiên
            Wallet adminWallet = walletService.getWalletByUserId(adminUser.getId());
            if (adminWallet == null) {
                // Tạo wallet nếu chưa có
                adminWallet = walletService.createWallet(adminUser.getId());
            }
            walletService.depositWithType(
                adminWallet.getWalletId(), 
                amountToPay, 
                com.sketchnotes.identityservice.enums.TransactionType.PURCHASE_AI_CREDITS, 
                "Admin revenue from AI credits purchase: " + creditPackage.getName()
            );
        }
        // 6. Cộng credits cho user
        Integer newBalance = previousBalance + creditPackage.getCreditAmount();
        user.setAiCredits(newBalance);
        userRepository.save(user);
        
        log.info("User {} credits updated: {} -> {}", userId, previousBalance, newBalance);
        
        // 7. Tạo transaction record
        String currency = creditPackage.getCurrency() != null ? creditPackage.getCurrency() : "VND";
        CreditTransaction transaction = CreditTransaction.builder()
                .user(user)
                .type(CreditTransactionType.PACKAGE_PURCHASE)
                .amount(creditPackage.getCreditAmount())
                .balanceAfter(newBalance)
                .description(String.format(
                        "Purchased package %s: %d credits for %s VND",
                        creditPackage.getName(),
                        creditPackage.getCreditAmount(),
                        amountToPay,
                        currency,
                        creditPackage.getSavingsAmount() != null ? creditPackage.getSavingsAmount() : BigDecimal.ZERO,
                        currency
                ))
                .referenceId("PKG-" + packageId)
                .build();
        
        CreditTransaction savedTransaction = creditTransactionRepository.save(transaction);
        log.info("Credit transaction created with ID: {}", savedTransaction.getId());
        
        // 8. Build và return response
        PurchasePackageResponse response = PurchasePackageResponse.builder()
                .transactionId(savedTransaction.getId())
                .transactionType(CreditTransactionType.PACKAGE_PURCHASE.name())
                .purchasedAt(LocalDateTime.now())
                .packageId(creditPackage.getId())
                .packageName(creditPackage.getName())
                .creditAmount(creditPackage.getCreditAmount())
                .amountPaid(amountToPay)
                .originalPrice(creditPackage.getOriginalPrice())
                .savedAmount(creditPackage.getSavingsAmount())
                .currency(creditPackage.getCurrency())
                .previousBalance(previousBalance)
                .newBalance(newBalance)
                .userId(user.getId())
                .email(user.getEmail())
                .message(String.format(
                        "Successfully purchased %d credits. Your new balance is %d credits.",
                        creditPackage.getCreditAmount(),
                        newBalance
                ))
                .build();
        
        log.info("User {} successfully purchased package {} ({} credits)", 
                userId, packageId, creditPackage.getCreditAmount());
        
        return response;
    }
    
    /**
     * Helper method để tính phần trăm giảm giá
     */
    private BigDecimal calculateDiscountPercent(BigDecimal originalPrice, BigDecimal discountedPrice) {
        if (originalPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal discount = originalPrice.subtract(discountedPrice);
        return discount.multiply(BigDecimal.valueOf(100))
                .divide(originalPrice, 2, RoundingMode.HALF_UP);
    }
    
    /**
     * Helper method để map Entity sang Response DTO
     */
    private CreditPackageResponse mapToResponse(CreditPackage creditPackage) {
        return CreditPackageResponse.builder()
                .id(creditPackage.getId())
                .name(creditPackage.getName())
                .description(creditPackage.getDescription())
                .creditAmount(creditPackage.getCreditAmount())
                .originalPrice(creditPackage.getOriginalPrice())
                .discountedPrice(creditPackage.getDiscountedPrice())
                .discountPercent(creditPackage.getDiscountPercent())
                .pricePerCredit(creditPackage.getPricePerCredit())
                .savingsAmount(creditPackage.getSavingsAmount())
                .currency(creditPackage.getCurrency())
                .isActive(creditPackage.getIsActive())
                .isPopular(creditPackage.getIsPopular())
                .displayOrder(creditPackage.getDisplayOrder())
                .createdAt(creditPackage.getCreatedAt())
                .updatedAt(creditPackage.getUpdatedAt())
                .build();
    }
}
