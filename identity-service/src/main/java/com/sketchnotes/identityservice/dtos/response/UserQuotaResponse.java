package com.sketchnotes.identityservice.dtos.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserQuotaResponse {

    private Integer maxProjects; // -1 means unlimited
    private Integer currentProjects;
    private Integer remainingProjects; // null if unlimited
    private String subscriptionType; // "Free", "Customer Pro", "Designer"
    private Boolean hasActiveSubscription;
    private Boolean canCreateProject;
}
