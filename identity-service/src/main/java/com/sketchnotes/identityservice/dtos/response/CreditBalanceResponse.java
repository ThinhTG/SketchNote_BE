package com.sketchnotes.identityservice.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO cho thông tin credit của user
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditBalanceResponse {
    
    private Long userId;
    private String email;
    private Integer currentBalance; // Số credit hiện tại
    private Integer totalPurchased; // Tổng credit đã mua
    private Integer totalUsed; // Tổng credit đã sử dụng
    private Long usageCount; // Số lần sử dụng AI
}
