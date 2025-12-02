package com.sketchnotes.identityservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity lưu trữ các gói Credit do admin config
 * Ví dụ: 50 credits - 50k, 100 credits - 95k (giảm 5%)
 */
@Entity
@Table(name = "credit_packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditPackage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name; // Tên gói: "Basic", "Standard", "Premium"
    
    @Column(length = 500)
    private String description; // Mô tả gói
    
    @Column(nullable = false)
    private Integer creditAmount; // Số lượng credits trong gói
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal originalPrice; // Giá gốc (VNĐ)
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal discountedPrice; // Giá sau giảm (VNĐ)
    
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercent; // Phần trăm giảm giá (0-100)
    
    @Column(length = 10)
    private String currency = "VND";
    
    @Column(nullable = false)
    private Boolean isActive = true; // Gói có đang hoạt động không
    
    @Column(nullable = false)
    private Boolean isPopular = false; // Đánh dấu gói được yêu thích/phổ biến
    
    @Column(nullable = false)
    private Integer displayOrder = 0; // Thứ tự hiển thị
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        // Tự động tính discountPercent nếu chưa có
        if (discountPercent == null && originalPrice != null && discountedPrice != null) {
            if (originalPrice.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal discount = originalPrice.subtract(discountedPrice);
                discountPercent = discount.multiply(BigDecimal.valueOf(100))
                        .divide(originalPrice, 2, java.math.RoundingMode.HALF_UP);
            } else {
                discountPercent = BigDecimal.ZERO;
            }
        }
    }
    
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Tính giá mỗi credit (sau giảm giá)
     */
    public BigDecimal getPricePerCredit() {
        if (creditAmount == null || creditAmount == 0) {
            return BigDecimal.ZERO;
        }
        return discountedPrice.divide(BigDecimal.valueOf(creditAmount), 2, java.math.RoundingMode.HALF_UP);
    }
    
    /**
     * Tính số tiền tiết kiệm được
     */
    public BigDecimal getSavingsAmount() {
        return originalPrice.subtract(discountedPrice);
    }
}
