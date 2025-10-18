package com.sketchnotes.order_service.config;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;


@Configuration
public class KafkaConfig {
    // Hàm helper để gửi event dễ hơn
    public static <T> void sendEvent(StreamBridge streamBridge, String bindingName, T payload) {
        Message<T> message = MessageBuilder.withPayload(payload).build();
        streamBridge.send(bindingName, message);
    }
}