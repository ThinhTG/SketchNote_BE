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
public class ProjectDetailResponse {
    private Long projectId;
    private String name;
    private String description;
    private String imageUrl;
    private Long ownerId;
    private String paperType;
    private boolean isOwner;
    private boolean isEdited;
    private boolean hasCollaboration;
    private List<PageResponse> pages;
}
