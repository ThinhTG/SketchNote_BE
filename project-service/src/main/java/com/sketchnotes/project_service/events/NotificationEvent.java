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
    private Long userId;
    private String title;
    private String message;
    private String type;
    private Long projectId;
    private Long resourceItemId;
    private Long orderId;
}
