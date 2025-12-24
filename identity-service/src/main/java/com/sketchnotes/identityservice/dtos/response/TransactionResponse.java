package com.sketchnotes.identityservice.dtos.response;

import com.sketchnotes.identityservice.enums.PaymentStatus;
import com.sketchnotes.identityservice.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    
    private Long transactionId;
    
    private Long orderId;
    
    private BigDecimal amount;
    
    private BigDecimal balance; // Balance after transaction
    
    private TransactionType type;
    
    private PaymentStatus status;
    
    private String provider;
    
    private String description;
    
    private LocalDateTime createdAt;
}
