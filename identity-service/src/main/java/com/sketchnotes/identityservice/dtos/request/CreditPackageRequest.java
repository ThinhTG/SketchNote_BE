package com.sketchnotes.identityservice.dtos.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO để tạo/cập nhật gói Credit
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditPackageRequest {
    
    @NotBlank(message = "Package name is required")
    @Size(max = 100, message = "Package name must not exceed 100 characters")
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @NotNull(message = "Credit amount is required")
    @Min(value = 1, message = "Credit amount must be at least 1")
    private Integer creditAmount;
    
    @NotNull(message = "Original price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Original price must be greater than 0")
    private BigDecimal originalPrice;
    
    @NotNull(message = "Discounted price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Discounted price must be at least 0")
    private BigDecimal discountedPrice;
    
    private Boolean isActive = true;
    
    private Boolean isPopular = false;
    
    private Integer displayOrder = 0;
}
