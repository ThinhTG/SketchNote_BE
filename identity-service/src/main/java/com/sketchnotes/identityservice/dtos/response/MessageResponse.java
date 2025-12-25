package com.sketchnotes.identityservice.dtos.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {
    private Long id;
    private Long senderId;
    private String senderName;
    private String senderAvatarUrl;
    private Long receiverId;
    private String receiverName;
    private String receiverAvatarUrl;
    private String content;
    private boolean isImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
