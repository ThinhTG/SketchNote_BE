package com.sketchnotes.project_service.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // endpoint client connect tới (SockJS fallback)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Fix: use setAllowedOriginPatterns instead of setAllowedOrigins
                .withSockJS();
        
        // Also add endpoint without SockJS for native WebSocket support
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // prefix cho destinations gửi từ client tới server (controller)
        config.setApplicationDestinationPrefixes("/app");
        // broker (simple in-memory). Cho public topics
        config.enableSimpleBroker("/topic", "/queue");
        // nếu scale, thay bằng RabbitMQ/ActiveMQ STOMP broker
    }
}