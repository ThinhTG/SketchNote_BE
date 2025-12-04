package com.sketchnotes.identityservice.dtos.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Request DTO for creating a withdrawal request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalRequestDto {
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotBlank(message = "Bank name is required")
    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    private String bankName;
    
    @NotBlank(message = "Bank account number is required")
    @Size(max = 50, message = "Bank account number must not exceed 50 characters")
    private String bankAccountNumber;
    
    @NotBlank(message = "Bank account holder name is required")
    @Size(max = 200, message = "Bank account holder name must not exceed 200 characters")
    private String bankAccountHolder;
}
