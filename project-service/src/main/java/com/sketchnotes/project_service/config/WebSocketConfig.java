package com.sketchnotes.project_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        log.info("🔵 [WebSocket] Registering STOMP endpoints...");
        
        // endpoint client connect tới (SockJS fallback)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request, 
                                                   ServerHttpResponse response,
                                                   WebSocketHandler wsHandler, 
                                                   Map<String, Object> attributes) {
                        log.info("🟢 [WebSocket] Handshake request from: {}", request.getRemoteAddress());
                        log.info("🟢 [WebSocket] Request URI: {}", request.getURI());
                        log.info("🟢 [WebSocket] Request headers: {}", request.getHeaders());
                        return true;
                    }

                    @Override
                    public void afterHandshake(ServerHttpRequest request, 
                                               ServerHttpResponse response,
                                               WebSocketHandler wsHandler, 
                                               Exception exception) {
                        if (exception != null) {
                            log.error("❌ [WebSocket] Handshake failed", exception);
                        } else {
                            log.info("✅ [WebSocket] Handshake completed successfully");
                        }
                    }
                })
                .withSockJS();
        
        // Also add endpoint without SockJS for native WebSocket support
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request, 
                                                   ServerHttpResponse response,
                                                   WebSocketHandler wsHandler, 
                                                   Map<String, Object> attributes) {
                        log.info("🟢 [WebSocket Native] Handshake request from: {}", request.getRemoteAddress());
                        return true;
                    }

                    @Override
                    public void afterHandshake(ServerHttpRequest request, 
                                               ServerHttpResponse response,
                                               WebSocketHandler wsHandler, 
                                               Exception exception) {
                        if (exception != null) {
                            log.error("❌ [WebSocket Native] Handshake failed", exception);
                        } else {
                            log.info("✅ [WebSocket Native] Handshake completed successfully");
                        }
        // broker (simple in-memory). Cho public topics
        config.enableSimpleBroker("/topic", "/queue");
        log.info("✅ [WebSocket] Simple broker enabled for: /topic, /queue");
        
        // nếu scale, thay bằng RabbitMQ/ActiveMQ STOMP broker
    }
}