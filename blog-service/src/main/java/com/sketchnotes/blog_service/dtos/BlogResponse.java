package com.sketchnotes.blog_service.dtos;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogResponse {
    private Long id;
    private String title;
    private String content;
    private Long authorId;
    private String authorDisplay;
    private Instant createdAt;
    private Instant updatedAt;
}
