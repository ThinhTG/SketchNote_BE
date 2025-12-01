package com.sketchnotes.project_service.controller;

import com.sketchnotes.project_service.dtos.socket.DrawMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

/**
 * WebSocket Controller for collaborative drawing
 * Handles real-time drawing events and broadcasts them to all users in a project
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class DrawingWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handle drawing messages from clients
     * Broadcast to all users in the same project
     * 
     * Client sends to: /app/draw/{projectId}
     * Server broadcasts to: /topic/draw/{projectId}
     */
    @MessageMapping("/draw/{projectId}")
    public void handleDrawingMessage(
            @DestinationVariable Long projectId,
            @Payload DrawMessage drawMessage) {
        
        drawMessage.setTimestamp(LocalDateTime.now());
        drawMessage.setProjectId(projectId);
        
        log.info("✏️ [Drawing] User {} drawing on project {}: {}", 
                drawMessage.getUserId(), projectId, drawMessage.getActionType());
        log.debug("📊 [Drawing] Data: color={}, tool={}, width={}", 
                drawMessage.getColor(), drawMessage.getToolType(), drawMessage.getStrokeWidth());
        
        String destination = "/topic/draw/" + projectId;
        log.info("🔊 [Drawing] BROADCASTING to: {}", destination);
        log.debug("📤 [Drawing] Message content: {}", drawMessage);
        
        // Broadcast to all users subscribed to this project's drawing topic
        try {
            messagingTemplate.convertAndSend(destination, drawMessage);
            log.info("✅ [Drawing] Message successfully sent to {}", destination);
        } catch (Exception e) {
            log.error("❌ [Drawing] Failed to broadcast message to {}: {}", destination, e.getMessage());
        }
    }

    /**
     * Handle undo/redo commands
     * 
     * Client sends to: /app/draw-undo-redo/{projectId}
     * Server broadcasts to: /topic/draw/{projectId}
     */
    @MessageMapping("/draw-undo-redo/{projectId}")
    public void handleUndoRedo(
            @DestinationVariable Long projectId,
            @Payload DrawMessage undoRedoMessage) {
        
        undoRedoMessage.setTimestamp(LocalDateTime.now());
        undoRedoMessage.setProjectId(projectId);
        
        String action = "UNDO".equals(undoRedoMessage.getActionType()) ? "UNDO" : "REDO";
        log.info("🔙 [Drawing] User {} performed {} on project {}", 
                undoRedoMessage.getUserId(), action, projectId);
        
        // Broadcast undo/redo action to all users
        messagingTemplate.convertAndSend(
                "/topic/draw/" + projectId,
                undoRedoMessage
        );
    }

    /**
     * Handle canvas clear command
     * 
     * Client sends to: /app/draw-clear/{projectId}
     * Server broadcasts to: /topic/draw/{projectId}
     */
    @MessageMapping("/draw-clear/{projectId}")
    public void handleCanvasClear(
            @DestinationVariable Long projectId,
            @Payload DrawMessage clearMessage) {
        
        clearMessage.setActionType("CLEAR");
        clearMessage.setTimestamp(LocalDateTime.now());
        clearMessage.setProjectId(projectId);
        
        log.warn("🗑️ [Drawing] User {} cleared canvas on project {}", 
                clearMessage.getUserId(), projectId);
        
        // Broadcast clear command to all users
        messagingTemplate.convertAndSend(
                "/topic/draw/" + projectId,
                clearMessage
        );
    }

    /**
     * Handle drawing sync request when user joins
     * (For catching up with current canvas state)
     * 
     * Client sends to: /app/draw-sync/{projectId}
     */
    @MessageMapping("/draw-sync/{projectId}")
    public void handleDrawingSyncRequest(
            @DestinationVariable Long projectId,
            @Payload DrawMessage syncRequest) {
        
        log.info("🔄 [Drawing] User {} requested sync for project {}", 
                syncRequest.getUserId(), projectId);
        
        // Note: Full canvas state should be fetched from database/cache (not implemented here)
        // This is just a placeholder for acknowledging the sync request
        
        syncRequest.setActionType("SYNC_REQUEST");
        syncRequest.setTimestamp(LocalDateTime.now());
        
        messagingTemplate.convertAndSend(
                "/topic/draw/" + projectId,
                syncRequest
        );
    }
}
