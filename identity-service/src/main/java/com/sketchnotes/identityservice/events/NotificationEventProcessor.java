package com.sketchnotes.identityservice.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sketchnotes.identityservice.dtos.request.CreateNotificationRequest;
import com.sketchnotes.identityservice.enums.NotificationType;
import com.sketchnotes.identityservice.service.interfaces.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for processing notification events
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventProcessor {
    
    private final INotificationService notificationService;
    private final ObjectMapper objectMapper;
    
    /**
     * Listen to notification events from Kafka
     * @param message JSON message from Kafka
     */
    @KafkaListener(topics = "notification-created", groupId = "notification-service-group")
    public void consumeNotificationEvent(String message) {
        try {
            log.info("Received notification event: {}", message);
            
            // Parse JSON message to NotificationEvent
            NotificationEvent event = objectMapper.readValue(message, NotificationEvent.class);
            
            log.info("Parsed event - userId: {}, type: '{}', projectId: {}", 
                    event.getUserId(), event.getType(), event.getProjectId());
            
            // Parse NotificationType with error handling
            NotificationType notificationType = null;
            if (event.getType() != null) {
                try {
                    String typeStr = event.getType().trim(); // Remove whitespace
                    notificationType = NotificationType.valueOf(typeStr);
                    log.debug("Successfully parsed NotificationType: {}", notificationType);
                } catch (IllegalArgumentException e) {
                    log.error("Invalid NotificationType: '{}'. Available types: {}", 
                            event.getType(), java.util.Arrays.toString(NotificationType.values()));
                    throw new RuntimeException("Invalid notification type: " + event.getType(), e);
                }
            }
            
            // Convert event to CreateNotificationRequest
            CreateNotificationRequest request = CreateNotificationRequest.builder()
                    .userId(event.getUserId())
                    .title(event.getTitle())
                    .message(event.getMessage())
                    .type(notificationType)
                    .projectId(event.getProjectId())
                    .resourceItemId(event.getResourceItemId())
                    .orderId(event.getOrderId())
                    .build();
            
            // Create notification
            notificationService.create(request);
            
            log.info("Successfully created notification for user {}", event.getUserId());
            
        } catch (Exception e) {
            log.error("Failed to process notification event: {}", e.getMessage(), e);
            // Don't throw exception to avoid message reprocessing
        }
    }
}
