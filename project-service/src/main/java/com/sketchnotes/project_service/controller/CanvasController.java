package com.sketchnotes.project_service.controller;

import com.sketchnotes.project_service.dtos.socket.CanvasAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CanvasController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/project/{projectId}/action")
    public void handleAction(
            @DestinationVariable Long projectId,
            CanvasAction action
    ) {
        log.info("📥📥📥 [Canvas] Received action for project: {}", projectId);
        log.info("📥 [Canvas] Action type: {}", action.getType());
        log.info("📥 [Canvas] Action tool: {}", action.getTool());
        log.info("📥 [Canvas] User ID: {}", action.getUserId());
        log.info("📥 [Canvas] Payload keys: {}", action.getPayload() != null ? action.getPayload().keySet() : "null");
        
        // broadcast cho tất cả user trong project
        String destination = "/topic/project/" + projectId;
        log.info("📤 [Canvas] Broadcasting to: {}", destination);
        
        try {
            messagingTemplate.convertAndSend(destination, action);
            log.info("✅✅✅ [Canvas] Broadcast successful to: {}", destination);
        } catch (Exception e) {
            log.error("❌❌❌ [Canvas] Broadcast failed", e);
        }
    }
}
