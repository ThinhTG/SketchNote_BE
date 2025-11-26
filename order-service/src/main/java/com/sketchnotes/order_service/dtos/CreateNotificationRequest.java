package com.sketchnotes.order_service.dtos;

import lombok.*;

/**
 * DTO for creating a notification.
 * Mirrors the CreateNotificationRequest from identity-service.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNotificationRequest {
    
    private Long userId;
    private String title;
    private String message;
    private String type; // NotificationType as String
    private Long resourceItemId;
    private Long orderId;
}
