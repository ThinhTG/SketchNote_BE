package com.sketchnotes.identityservice.dtos.request;

import com.sketchnotes.identityservice.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Request DTO for creating a new notification.
 * Used by internal services and admin endpoints.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNotificationRequest {
    
    /**
     * ID of the user who will receive the notification
     */
    @NotNull(message = "User ID is required")
    private Long userId;
    
    /**
     * Notification title
     */
    @NotBlank(message = "Title is required")
    private String title;
    
    /**
     * Notification message content
     */
    @NotBlank(message = "Message is required")
    private String message;
    
    /**
     * Type of notification
     */
    private NotificationType type;
    
    /**
     * Optional resource/template reference
     */
    private Long resourceItemId;
    
    /**
     * Optional order reference
     */
    private Long orderId;
}
