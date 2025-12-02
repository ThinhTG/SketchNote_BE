package com.sketchnotes.identityservice.repository;

import com.sketchnotes.identityservice.model.CreditPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreditPackageRepository extends JpaRepository<CreditPackage, Long> {
    
    /**
     * Lấy tất cả gói đang active, sắp xếp theo thứ tự hiển thị
     */
    List<CreditPackage> findByIsActiveTrueOrderByDisplayOrderAsc();
    
    /**
     * Lấy tất cả gói, sắp xếp theo thứ tự hiển thị (cho admin)
     */
    List<CreditPackage> findAllByOrderByDisplayOrderAsc();
    
    /**
     * Tìm gói theo số lượng credits
     */
    Optional<CreditPackage> findByCreditAmountAndIsActiveTrue(Integer creditAmount);
    
    /**
     * Kiểm tra gói có tồn tại với tên này không
     */
    boolean existsByNameIgnoreCase(String name);
    
    /**
     * Kiểm tra gói có tồn tại với số credits này không
     */
    boolean existsByCreditAmount(Integer creditAmount);
    
    /**
     * Lấy gói phổ biến nhất
     */
    List<CreditPackage> findByIsPopularTrueAndIsActiveTrueOrderByDisplayOrderAsc();
}
