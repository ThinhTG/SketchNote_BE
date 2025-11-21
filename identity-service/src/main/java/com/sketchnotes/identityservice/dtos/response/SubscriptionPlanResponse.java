package com.sketchnotes.identityservice.dtos.response;

import com.sketchnotes.identityservice.enums.PlanType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlanResponse {

    private Long planId;
    private String planName;
    private PlanType planType;
    private BigDecimal price;
    private String currency;
    private Integer durationDays;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
