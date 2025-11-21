package com.sketchnotes.identityservice.dtos.response;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class UserResponse  implements Serializable {
    private Long id;
    private  String keycloakId;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String avatarUrl;
    
    // Subscription information
    private Boolean hasActiveSubscription;
    private String subscriptionType;  // "Free", "Customer Pro - Monthly", "Designer - Yearly", etc.
    private LocalDateTime subscriptionEndDate;
    private Integer maxProjects;  // -1 for unlimited, 3 for free tier
}
