package com.sketchnotes.project_service.dtos.response;

import com.sketchnotes.project_service.enums.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Response DTO for notification data
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {
    
    private Long id;
    private Long userId;
    private String title;
    private String message;
    private NotificationType type;
    private Boolean isRead;
    private Long projectId;
    private LocalDateTime createdAt;
}
