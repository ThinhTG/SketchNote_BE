package com.sketchnotes.project_service.dtos.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserQuotaResponse {
    private Integer maxProjects;
    private Integer currentProjects;
    private Integer remainingProjects;
    private String subscriptionType;
    private Boolean hasActiveSubscription;
    private Boolean canCreateProject;
}
