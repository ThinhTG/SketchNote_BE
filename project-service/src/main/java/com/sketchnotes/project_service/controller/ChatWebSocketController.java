package com.sketchnotes.project_service.controller;

import com.sketchnotes.project_service.dtos.socket.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.private")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        chatMessage.setTimestamp(LocalDateTime.now());
        
        // Gửi cho người nhận
        messagingTemplate.convertAndSend(
                "/queue/private/" + chatMessage.getReceiverId(), 
                chatMessage
        );
        
        // Gửi lại cho người gửi (confirmation)
        messagingTemplate.convertAndSend(
                "/queue/private/" + chatMessage.getSenderId(), 
                chatMessage
        );
    }
}
