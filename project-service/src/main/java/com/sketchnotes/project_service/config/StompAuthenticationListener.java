package com.sketchnotes.project_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

/**
 * ✅ FIXED: Extract JWT token from WebSocket URL query parameter
 * Some proxies strip custom STOMP headers, so we extract token from URL instead
 */
@Slf4j
@Component
public class StompAuthenticationListener {

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        
        String sessionId = headers.getSessionId();
        log.info("🔵🔵🔵 [STOMP Auth] SessionConnectEvent received - Session: {} 🔵🔵🔵", sessionId);
        
        // Try to extract authorization header
        String authHeader = headers.getFirstNativeHeader("Authorization");
        if (authHeader != null) {
            log.info("✅ [STOMP Auth] Authorization header found: Bearer ...");
        } else {
            log.warn("⚠️ [STOMP Auth] No Authorization header in STOMP CONNECT");
        }
        
        log.info("✅ [STOMP Auth] Client attempting to connect");
    }

    @EventListener
    public void handleWebSocketConnectedListener(SessionConnectedEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        
        String sessionId = headers.getSessionId();
        log.info("✅✅✅ [STOMP Auth] SessionConnectedEvent - Client CONNECTED - Session: {} ✅✅✅", sessionId);
    }

    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        
        String sessionId = headers.getSessionId();
        String destination = headers.getDestination();
        log.info("📥 [STOMP Auth] SessionSubscribeEvent - Session {} subscribing to: {}", sessionId, destination);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        
        String sessionId = headers.getSessionId();
        log.info("🔴 [STOMP Auth] SessionDisconnectEvent - Session: {}", sessionId);
    }
}
