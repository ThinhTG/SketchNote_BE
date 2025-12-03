package com.sketchnotes.identityservice.dtos.response.admin;

import com.sketchnotes.identityservice.enums.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO Response cho danh sách User Subscription của Admin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserSubscriptionResponse {
    private Long subscriptionId;
    private Long userId;
    private String userEmail;
    private String userName;
    
    // Subscription Plan info
    private Long planId;
    private String planName;
    private String planType;
    private BigDecimal price;
    private String currency;
    
    // Subscription details
    private SubscriptionStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean autoRenew;
    private LocalDateTime createdAt;
}
