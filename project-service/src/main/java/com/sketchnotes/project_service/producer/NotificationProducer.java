package com.sketchnotes.project_service.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka message producer for sending events
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String TOPIC = "notification-created";
    

    public void sendNotificationEvent(Object event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, message);
        } catch (Exception e) {
            log.error("Failed to send notification event to Kafka: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Send notification event with custom topic
     * @param topic custom topic name
     * @param event notification event object
     */
    public void sendNotificationEvent(String topic, Object event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, message);
            log.info("Sent notification event to Kafka topic: {}", topic);
        } catch (Exception e) {
            log.error("Failed to send notification event to Kafka topic {}: {}", topic, e.getMessage(), e);
        }
    }
}
