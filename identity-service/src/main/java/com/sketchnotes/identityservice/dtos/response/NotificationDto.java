package com.sketchnotes.identityservice.dtos.response;

import com.sketchnotes.identityservice.enums.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for notification responses sent to clients.
 * Contains all notification information for display purposes.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {
    
    private Long id;
    
    private String title;
    
    private String message;
    
    private NotificationType type;
    
    private Long resourceItemId;
    
    private Long orderId;
    
    private Long projectId;
    
    private boolean isRead;
    
    private LocalDateTime createdAt;
}
