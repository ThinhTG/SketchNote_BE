package com.sketchnotes.project_service.dtos.request;

import com.sketchnotes.project_service.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Request DTO for creating a new notification.
 * Used for internal service-to-service communication.
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
    private String type;
    
    /**
     * Optional project reference
     */
    private Long projectId;
}
