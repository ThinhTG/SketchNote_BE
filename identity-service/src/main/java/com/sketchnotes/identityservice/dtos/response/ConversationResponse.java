package com.sketchnotes.identityservice.dtos.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationResponse {
    private Long userId;
    private String userName;
    private String userAvatarUrl;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
}
