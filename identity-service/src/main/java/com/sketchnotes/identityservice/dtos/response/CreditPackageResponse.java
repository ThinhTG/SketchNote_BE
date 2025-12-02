package com.sketchnotes.identityservice.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO cho gói Credit
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditPackageResponse {
    
    private Long id;
    private String name;
    private String description;
    private Integer creditAmount;
    private BigDecimal originalPrice;
    private BigDecimal discountedPrice;
    private BigDecimal discountPercent;
    private BigDecimal pricePerCredit;
    private BigDecimal savingsAmount;
    private String currency;
    private Boolean isActive;
    private Boolean isPopular;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
