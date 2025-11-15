package com.sketchnotes.project_service.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCollaborationResponse {
    private Long projectId;
    private Long userId;
    private String email;
    private boolean isEdited;
    private LocalDateTime createdAt;
}
