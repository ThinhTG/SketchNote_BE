package com.sketchnotes.project_service.events;

import lombok.*;

/**
 * Event sent to Kafka when a notification needs to be created
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent {
    
    /**
     * ID of the user who will receive the notification
     */
    private Long userId;
    
    /**
     * Notification title
     */
    private String title;
    
    /**
     * Notification message content
     */
    private String message;
    
    /**
     * Type of notification (as String to avoid enum serialization issues)
     */
    private String type;
    
    /**
     * Optional project reference
     */
    private Long projectId;
    
    /**
     * Optional resource/template reference
     */
    private Long resourceItemId;
    
    /**
     * Optional order reference
     */
    private Long orderId;
}
