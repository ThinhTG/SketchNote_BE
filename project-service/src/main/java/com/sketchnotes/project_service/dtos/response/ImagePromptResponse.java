package com.sketchnotes.project_service.dtos.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImagePromptResponse {
    private Long imagePromptId;
    private String imageUrl;
    private LocalDateTime createdAt;
}
