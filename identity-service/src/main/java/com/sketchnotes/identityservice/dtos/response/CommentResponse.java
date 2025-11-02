package com.sketchnotes.identityservice.dtos.response;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {
    private Long id;
    private Long postId;
    private String content;
    private Long authorId;
    private String authorDisplay;
    private Long parentCommentId;
    private Instant createdAt;
}
