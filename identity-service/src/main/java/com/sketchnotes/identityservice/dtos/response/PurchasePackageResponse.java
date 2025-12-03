package com.sketchnotes.identityservice.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO cho việc mua gói credit package
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchasePackageResponse {
    
    // Thông tin giao dịch
    private Long transactionId;
    private String transactionType;
    private LocalDateTime purchasedAt;
    
    // Thông tin gói đã mua
    private Long packageId;
    private String packageName;
    private Integer creditAmount;
    private BigDecimal amountPaid; // Số tiền đã thanh toán (discountedPrice)
    private BigDecimal originalPrice; // Giá gốc
    private BigDecimal savedAmount; // Số tiền tiết kiệm được
    private String currency;
    
    // Thông tin số dư credit sau khi mua
    private Integer previousBalance;
    private Integer newBalance;
    
    // Thông tin user
    private Long userId;
    private String email;
    
    // Message
    private String message;
}
