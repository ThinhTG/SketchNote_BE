package com.sketchnotes.project_service.controller;

import com.sketchnotes.project_service.dtos.socket.CanvasAction;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class CanvasController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/project/{projectId}/action")
    public void handleAction(
            @DestinationVariable Long projectId,
            CanvasAction action
    ) {
        // broadcast cho tất cả user trong project
        messagingTemplate.convertAndSend(
                "/topic/project/" + projectId,
                action
        );
    }
}
