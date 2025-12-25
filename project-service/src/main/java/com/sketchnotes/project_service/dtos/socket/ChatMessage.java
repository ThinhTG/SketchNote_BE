package com.sketchnotes.project_service.dtos.socket;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    private Long senderId;
    private String senderName;
    private String senderAvatarUrl;
    private Long receiverId;
    private String content;
    private boolean isImage;
    private LocalDateTime timestamp;
}
