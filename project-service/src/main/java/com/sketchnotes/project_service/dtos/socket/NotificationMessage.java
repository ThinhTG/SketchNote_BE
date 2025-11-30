package com.sketchnotes.project_service.dtos.socket;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Message for real-time notifications via WebSocket
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationMessage {
    private Long notificationId;
    private Long recipientId;           // User who receives the notification
    private Long senderId;              // User who triggers the notification
    private String senderName;
    private String senderAvatarUrl;
    
    // Notification details
    private String type;                // "PROJECT_INVITE", "COLLAB_START", "DRAWING_UPDATE", "COMMENT", etc.
    private String title;
    private String content;             // Main message content
    private String link;                // Navigation link (optional)
    private String icon;                // Icon URL or icon name (optional)
    
    // Related entity information
    private Long projectId;             // Related project
    private Long collaborationId;       // Related collaboration record
    
    // Status
    private Boolean read = false;
    private LocalDateTime timestamp;
    private LocalDateTime readAt;
    
    // Priority
    private String priority;            // "LOW", "NORMAL", "HIGH"
}
