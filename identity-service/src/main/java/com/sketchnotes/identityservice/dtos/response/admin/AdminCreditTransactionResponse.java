package com.sketchnotes.identityservice.dtos.response.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sketchnotes.identityservice.enums.CreditTransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO Response cho danh sách Credit Transaction của Admin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCreditTransactionResponse {
    private Long id;
    private Long userId;
    private String userEmail;
    private String userName;
    private CreditTransactionType type;
    private Integer amount;
    private Integer balanceAfter;
    private String description;
    private String referenceId;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
