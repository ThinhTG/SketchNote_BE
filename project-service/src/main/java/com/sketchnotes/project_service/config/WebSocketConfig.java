package com.sketchnotes.project_service.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompChannelInterceptor stompChannelInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        log.info("🔵 [WebSocket] Registering STOMP endpoints...");
        
        // Native WebSocket endpoint (no SockJS)
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
                });
        
        log.info("✅ [WebSocket] Native STOMP endpoint registered at /ws");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        log.info("🔵 [WebSocket] Registering STOMP channel interceptor...");
        registration.interceptors(stompChannelInterceptor);
        log.info("✅ [WebSocket] STOMP channel interceptor registered");
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        // Set to MAX_VALUE (~2GB) as requested
        registry.setMessageSizeLimit(Integer.MAX_VALUE); 
        registry.setSendTimeLimit(20 * 10000);
        registry.setSendBufferSizeLimit(Integer.MAX_VALUE);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        log.info("🔵 [WebSocket] Configuring message broker...");
        
        // prefix cho destinations gửi từ client tới server (controller)
        config.setApplicationDestinationPrefixes("/app");
        log.info("✅ [WebSocket] Application destination prefix: /app");
        
        // broker (simple in-memory). Cho public topics
        // Topics:
        // - /topic/chat/{chatRoomId} - Chat messages
        // - /topic/draw/{projectId} - Collaborative drawing events
        // - /topic/notify/{projectId} - Project-wide notifications
        // Queues:
        // - /queue/private/{userId} - Private messages (1:1 chat)
        // - /queue/notify/{userId} - Private notifications to specific user
        config.enableSimpleBroker("/topic", "/queue");
        log.info("✅ [WebSocket] Simple broker enabled for: /topic (chat, draw, notify), /queue (private messages, notifications)");
    }
}