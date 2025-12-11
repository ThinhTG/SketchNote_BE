package com.sketchnotes.identityservice.dtos.response.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sketchnotes.identityservice.enums.PaymentStatus;
import com.sketchnotes.identityservice.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO Response cho danh sách Transaction của Admin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminTransactionResponse {
    private Long transactionId;
    private Long walletId;
    private Long userId;
    private String userEmail;
    private Long orderId;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private TransactionType type;
    private PaymentStatus status;
    private String provider;
    private String externalTransactionId;
    private String description;
    private Long orderCode;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
