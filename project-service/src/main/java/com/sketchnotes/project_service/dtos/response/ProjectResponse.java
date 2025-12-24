package com.sketchnotes.project_service.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    private Long projectId;
    private String name;
    private String description;
    private String imageUrl;
    private Long ownerId;
    private boolean isOwner;
    private boolean isEdited;
    private String paperSize;
    private boolean isAccepted;
    private List<PageResponse> pages;
}