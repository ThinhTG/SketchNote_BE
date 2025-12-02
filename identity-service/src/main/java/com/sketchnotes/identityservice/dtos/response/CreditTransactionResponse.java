package com.sketchnotes.identityservice.dtos.response;

import com.sketchnotes.identityservice.enums.CreditTransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO cho lịch sử giao dịch credit
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditTransactionResponse {
    
    private Long id;
    private CreditTransactionType type;
    private Integer amount;
    private Integer balanceAfter;
    private String description;
    private String referenceId;
    private LocalDateTime createdAt;
}
