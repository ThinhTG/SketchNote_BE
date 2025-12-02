package com.sketchnotes.project_service.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO cho thông tin credit balance
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditBalanceResponse {
    
    private Long userId;
    private String email;
    private Integer currentBalance;
    private Integer totalPurchased;
    private Integer totalUsed;
    private Long usageCount;
}
