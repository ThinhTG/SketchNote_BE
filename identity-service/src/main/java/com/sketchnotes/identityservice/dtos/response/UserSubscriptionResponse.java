package com.sketchnotes.identityservice.dtos.response;

import com.sketchnotes.identityservice.enums.SubscriptionStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSubscriptionResponse {

    private Long subscriptionId;
    private Long userId;
    private SubscriptionPlanResponse plan;
    private SubscriptionStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean autoRenew;
    private String transactionId;
    private LocalDateTime createdAt;
    private Boolean isCurrentlyActive;
}
