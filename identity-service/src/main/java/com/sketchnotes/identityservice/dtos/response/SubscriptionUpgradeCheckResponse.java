package com.sketchnotes.identityservice.dtos.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for subscription upgrade check response
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionUpgradeCheckResponse {
    
    /**
     * Whether upgrade is allowed
     */
    private boolean canUpgrade;
    
    /**
     * Whether user has an active subscription that will be replaced
     */
    private boolean hasActiveSubscription;
    
    /**
     * Warning message if user has active subscription
     */
    private String warningMessage;
    
    /**
     * Current subscription details (if any)
     */
    private CurrentSubscriptionInfo currentSubscription;
    
    /**
     * New plan details
     */
    private NewPlanInfo newPlan;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CurrentSubscriptionInfo {
        private Long subscriptionId;
        private String planName;
        private String planType;
        private LocalDateTime endDate;
        private Integer remainingDays;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NewPlanInfo {
        private Long planId;
        private String planName;
        private String planType;
        private BigDecimal price;
        private Integer durationDays;
    }
}
