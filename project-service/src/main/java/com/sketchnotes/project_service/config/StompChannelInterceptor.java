package com.sketchnotes.project_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StompChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null) {
            StompCommand command = accessor.getCommand();
            
            log.info("🔵 [STOMP] Command: {}, Session: {}", command, accessor.getSessionId());
            
            if (StompCommand.CONNECT.equals(command)) {
                log.info("🟢 [STOMP] CONNECT frame received");
                log.info("🟢 [STOMP] Headers: {}", accessor.toNativeHeaderMap());
                
                // Extract Authorization header
                String authHeader = accessor.getFirstNativeHeader("Authorization");
                if (authHeader != null) {
                    log.info("✅ [STOMP] Authorization header present: {}", 
                            authHeader.substring(0, Math.min(20, authHeader.length())) + "...");
                } else {
                    log.warn("⚠️ [STOMP] No Authorization header found");
                }
                
                // Allow connection to proceed
                log.info("✅ [STOMP] Allowing CONNECT to proceed");
            }
            
            if (StompCommand.DISCONNECT.equals(command)) {
                log.info("🔴 [STOMP] DISCONNECT frame received for session: {}", accessor.getSessionId());
            }
            
            if (StompCommand.SUBSCRIBE.equals(command)) {
                String destination = accessor.getDestination();
                String sessionId = accessor.getSessionId();
                log.info("📥 [STOMP] SUBSCRIBE from session {} to: {}", sessionId, destination);
                
                if (destination != null && destination.startsWith("/topic/draw/")) {
                    log.info("🎨 [STOMP] User subscribed to drawing topic: {}", destination);
                }
            }
            
            if (StompCommand.SEND.equals(command)) {
                String destination = accessor.getDestination();
                String sessionId = accessor.getSessionId();
                log.info("📤 [STOMP] SEND from session {} to: {}", sessionId, destination);
                
                if (destination != null && destination.startsWith("/app/draw/")) {
                    log.info("🎨 [STOMP] Drawing message received from client: {}", destination);
                }
            }
        }
        
        return message;
    }

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && StompCommand.CONNECTED.equals(accessor.getCommand())) {
            log.info("✅✅✅ [STOMP] CONNECTED frame sent to client ✅✅✅");
        }
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        if (ex != null) {
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
            log.error("❌ [STOMP] Error sending message. Command: {}, Error: {}", 
                    accessor != null ? accessor.getCommand() : "UNKNOWN", ex.getMessage(), ex);
        }
    }
}
