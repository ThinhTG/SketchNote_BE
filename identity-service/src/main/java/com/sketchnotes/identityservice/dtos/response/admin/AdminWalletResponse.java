package com.sketchnotes.identityservice.dtos.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO Response cho danh sách Wallet của Admin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminWalletResponse {
    private Long walletId;
    private Long userId;
    private String userEmail;
    private String userName;
    private BigDecimal balance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
