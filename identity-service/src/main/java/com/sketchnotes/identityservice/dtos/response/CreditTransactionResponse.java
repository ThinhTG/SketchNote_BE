package com.sketchnotes.identityservice.dtos.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private String packageName; // Tên gói (nếu là giao dịch mua gói)
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
