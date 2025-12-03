package com.sketchnotes.identityservice.service;

import com.sketchnotes.identityservice.dtos.request.CreditPackageRequest;
import com.sketchnotes.identityservice.dtos.response.CreditBalanceResponse;
import com.sketchnotes.identityservice.dtos.response.CreditPackageResponse;
import com.sketchnotes.identityservice.dtos.response.PurchasePackageResponse;

import java.util.List;

/**
 * Interface cho Credit Package Service
 */
public interface ICreditPackageService {
    
    /**
     * Tạo gói credit mới (Admin)
     */
    CreditPackageResponse createPackage(CreditPackageRequest request);
    
    /**
     * Cập nhật gói credit (Admin)
     */
    CreditPackageResponse updatePackage(Long id, CreditPackageRequest request);
    
    /**
     * Xóa gói credit (Admin) - soft delete bằng cách set isActive = false
     */
    void deletePackage(Long id);
    
    /**
     * Lấy thông tin một gói credit theo ID
     */
    CreditPackageResponse getPackageById(Long id);
    
    /**
     * Lấy tất cả gói credit đang active (cho User)
     */
    List<CreditPackageResponse> getActivePackages();
    
    /**
     * Lấy tất cả gói credit (cho Admin)
     */
    List<CreditPackageResponse> getAllPackages();
    
    /**
     * Kích hoạt/Vô hiệu hóa gói credit (Admin)
     */
    CreditPackageResponse togglePackageStatus(Long id);
    
    /**
     * Mua gói credit package (User)
     * @param userId ID của user
     * @param packageId ID của gói credit package
     * @return PurchasePackageResponse chứa thông tin giao dịch và số dư mới
     */
    PurchasePackageResponse purchasePackage(Long userId, Long packageId);
}
