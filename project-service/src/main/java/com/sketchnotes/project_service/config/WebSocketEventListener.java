package com.sketchnotes.project_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;

@Slf4j
@Component
public class WebSocketEventListener {

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        log.info("✅✅✅ [WebSocket Event] NEW CONNECTION ✅✅✅");
        log.info("✅ [WebSocket Event] Session ID: {}", headerAccessor.getSessionId());
        log.info("✅ [WebSocket Event] User: {}", headerAccessor.getUser());
        log.info("✅ [WebSocket Event] Session attributes: {}", headerAccessor.getSessionAttributes());
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        log.info("🔴 [WebSocket Event] CONNECTION CLOSED");
        log.info("🔴 [WebSocket Event] Session ID: {}", headerAccessor.getSessionId());
        log.info("🔴 [WebSocket Event] User: {}", headerAccessor.getUser());
    }

    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        log.info("📥 [WebSocket Event] NEW SUBSCRIPTION");
        log.info("📥 [WebSocket Event] Session ID: {}", headerAccessor.getSessionId());
        log.info("📥 [WebSocket Event] Destination: {}", headerAccessor.getDestination());
        log.info("📥 [WebSocket Event] Subscription ID: {}", headerAccessor.getSubscriptionId());
        log.info("📥 [WebSocket Event] User: {}", headerAccessor.getUser());
    }

    @EventListener
    public void handleWebSocketUnsubscribeListener(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        log.info("🔴 [WebSocket Event] UNSUBSCRIBE");
        log.info("🔴 [WebSocket Event] Session ID: {}", headerAccessor.getSessionId());
        log.info("🔴 [WebSocket Event] Subscription ID: {}", headerAccessor.getSubscriptionId());
    }
}
