package com.sketchnotes.identityservice.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseSubscriptionRequest {

    @NotNull(message = "Plan ID is required")
    private Long planId;

    private Boolean autoRenew = false;
    
    /**
     * Set to true to confirm upgrade when user has an active subscription
     * that has not expired yet
     */
    private Boolean confirmUpgrade = false;
}
