package com.sketchnotes.project_service.dtos.mapper;

import com.sketchnotes.project_service.dtos.response.ProjectVersionResponse;
import com.sketchnotes.project_service.entity.ProjectVersion;

import java.util.stream.Collectors;

public class ProjectVersionMapper {
    
    public static ProjectVersionResponse toDTO(ProjectVersion entity) {
        if (entity == null) {
            return null;
        }
        
        return ProjectVersionResponse.builder()
                .projectVersionId(entity.getProjectVersionId())
                .versionNumber(entity.getVersionNumber())
                .note(entity.getNote())
                .projectId(entity.getProject() != null ? entity.getProject().getProjectId() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .pages(entity.getPages() != null ? 
                    entity.getPages().stream()
                        .filter(page -> page.getDeletedAt() == null)
                        .map(PageMapper::toDTO)
                        .collect(Collectors.toList()) : null)
                .build();
    }
}
