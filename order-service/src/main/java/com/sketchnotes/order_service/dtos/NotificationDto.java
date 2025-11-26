package com.sketchnotes.order_service.dtos;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for notification responses.
 * Mirrors the NotificationDto from identity-service.
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
    private String type;
    private Long resourceItemId;
    private Long orderId;
    private boolean isRead;
    private LocalDateTime createdAt;
}
