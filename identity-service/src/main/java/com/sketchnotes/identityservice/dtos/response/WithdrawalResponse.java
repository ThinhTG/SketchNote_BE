package com.sketchnotes.identityservice.dtos.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sketchnotes.identityservice.enums.WithdrawalStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for withdrawal request details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalResponse {
    
    private Long id;
    private Long userId;
    private BigDecimal amount;
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountHolder;
    private WithdrawalStatus status;
    private Long staffId;
    private String rejectionReason;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
