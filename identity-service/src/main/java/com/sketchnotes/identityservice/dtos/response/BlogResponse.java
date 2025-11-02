package com.sketchnotes.identityservice.dtos.response;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogResponse {
    private Long id;
    private String title;
    private Long authorId;
    private  String summary;
    private String authorDisplay;
    private String imageUrl;
    private Instant createdAt;
    private Instant updatedAt;
    private List<ContentResponse> contents;
}
