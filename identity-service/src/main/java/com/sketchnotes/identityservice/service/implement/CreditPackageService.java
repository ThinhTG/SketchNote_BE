package com.sketchnotes.identityservice.service.implement;

import com.sketchnotes.identityservice.dtos.request.CreditPackageRequest;
import com.sketchnotes.identityservice.dtos.response.CreditPackageResponse;
import com.sketchnotes.identityservice.exception.AppException;
import com.sketchnotes.identityservice.exception.ErrorCode;
import com.sketchnotes.identityservice.model.CreditPackage;
import com.sketchnotes.identityservice.repository.CreditPackageRepository;
import com.sketchnotes.identityservice.service.ICreditPackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation cho quản lý Credit Packages
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreditPackageService implements ICreditPackageService {
    
    private final CreditPackageRepository creditPackageRepository;
    
    @Override
    @Transactional
    public CreditPackageResponse createPackage(CreditPackageRequest request) {
        log.info("Creating new credit package: {}", request.getName());
        
        // Validate giá giảm không được lớn hơn giá gốc
        if (request.getDiscountedPrice().compareTo(request.getOriginalPrice()) > 0) {
            throw new AppException(ErrorCode.INVALID_PRICE);
        }
        
        // Tính phần trăm giảm giá
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
        log.info("Credit package created successfully with ID: {}", saved.getId());
        
        return mapToResponse(saved);
    }
    
    @Override
    @Transactional
    public CreditPackageResponse updatePackage(Long id, CreditPackageRequest request) {
        log.info("Updating credit package ID: {}", id);
        
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
        log.info("Credit package updated successfully: {}", id);
        
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
