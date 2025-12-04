package com.sketchnotes.identityservice.dtos.request;

import lombok.*;

/**
 * Request DTO for rejecting a withdrawal with optional reason.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectWithdrawalRequest {
    
    private String rejectionReason;
}
