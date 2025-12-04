package com.sketchnotes.identityservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time notifications.
 * Configures STOMP over WebSocket with SockJS fallback.
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Value("${websocket.allowed-origins:http://localhost:3000,http://34.126.134.243:8888}")
    private String allowedOrigins;
    
    /**
     * Configure message broker for pub/sub messaging.
     * - /topic: for broadcasting to multiple subscribers
     * - /queue: for point-to-point messaging
     * - /app: prefix for messages from client to server
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple in-memory broker for /topic and /queue destinations
        config.enableSimpleBroker("/topic", "/queue");
        
        // Prefix for messages from client to server (not used for push-only notifications)
        config.setApplicationDestinationPrefixes("/app");
        
        // Prefix for user-specific destinations (optional, for principal-based messaging)
        config.setUserDestinationPrefix("/user");
    }
    
    /**
     * Register STOMP endpoints for WebSocket connections.
     * Clients connect to /ws-notifications to establish WebSocket connection.
     * Native WebSocket only (no SockJS fallback) for better performance.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        log.info("🔵 [Notification WebSocket] ===== REGISTERING STOMP ENDPOINTS =====");
        log.info("🔵 [Notification WebSocket] Registering endpoint: /ws-notifications");
        
        registry.addEndpoint("/ws-notifications")
                .setAllowedOriginPatterns(allowedOrigins.split(","));
                // SockJS removed - using native WebSocket only
        
        log.info("✅ [Notification WebSocket] Endpoint registered successfully at /ws-notifications");
        log.info("✅ [Notification WebSocket] Allowed origins: {}", allowedOrigins);
        log.info("🔵 [Notification WebSocket] ===== STOMP ENDPOINTS REGISTERED =====");
    }
}
