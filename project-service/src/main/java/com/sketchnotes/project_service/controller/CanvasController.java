package com.sketchnotes.project_service.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import com.sketchnotes.project_service.dtos.socket.CanvasAction;
import com.sketchnotes.project_service.service.RealtimeMessageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CanvasController {

    private final RealtimeMessageService realtimeMessageService;

    /**
     * Handle stroke/drawing actions only
     * Client sends to: /app/project/{projectId}/stroke
     * Server broadcasts to: /topic/project/{projectId}/stroke
     * 
     * *** CRITICAL FIX: Now adds sequence numbers for proper ordering ***
     */
    @MessageMapping("/project/{projectId}/stroke")
    public void handleAction(
            @DestinationVariable Long projectId,
            CanvasAction action
    ) {
        log.debug("📥 [Canvas] Received stroke action for project: {} type: {}", projectId, action.getType());
        
        // Build message with sequence number (will be assigned by RealtimeMessageService)
        Map<String, Object> message = new HashMap<>();
        message.put("type", action.getType());
        message.put("tool", action.getTool());
        message.put("userId", action.getUserId());
        message.put("payload", action.getPayload());
        message.put("version", realtimeMessageService.getCurrentVersion(projectId));
        message.put("timestamp", System.currentTimeMillis());
        
        // Set custom destination for stroke route
        String destination = "/topic/project/" + projectId + "/stroke";
        message.put("_destination", destination);
        
        // Use async broadcast via RealtimeMessageService
        boolean queued = realtimeMessageService.enqueueMessage(projectId, action.getUserId(), message);
        
        if (!queued) {
            log.warn("📤 [Canvas] Message rate-limited or queue full for project {}", projectId);
        } else {
            log.debug("📤 [Canvas] Queued stroke to: {}", destination);
        }
    }
}
