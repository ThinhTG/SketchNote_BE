package com.sketchnotes.identityservice.model;

import com.sketchnotes.identityservice.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity representing a notification sent to a user.
 * Supports various notification types and can reference related entities (orders, resources).
 */
@Entity
@Table(name = "notification", indexes = {
    @Index(name = "idx_notification_user", columnList = "user_id"),
    @Index(name = "idx_notification_isread", columnList = "user_id, is_read"),
    @Index(name = "idx_notification_created", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * ID of the user who will receive this notification
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * Short title/subject of the notification
     */
    @Column(nullable = false, length = 255)
    private String title;
    
    /**
     * Detailed message content
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;
    
    /**
     * Type/category of notification
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private NotificationType type;
    
    /**
     * Optional reference to a resource/template (nullable)
     */
    @Column(name = "resource_item_id")
    private Long resourceItemId;
    
    /**
     * Optional reference to an order (nullable)
     */
    @Column(name = "order_id")
    private Long orderId;
    
    /**
     * Optional reference to a project (nullable)
     */
    @Column(name = "project_id")
    private Long projectId;
    
    /**
     * Whether the user has read this notification
     */
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean isRead = false;
    
    /**
     * Timestamp when notification was created
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
