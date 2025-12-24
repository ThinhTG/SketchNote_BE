package com.sketchnotes.identityservice.events;

import lombok.*;

/**
 * Event received from Kafka when a notification needs to be created
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
