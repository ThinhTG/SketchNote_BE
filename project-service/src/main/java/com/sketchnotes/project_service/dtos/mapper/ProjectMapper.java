package com.sketchnotes.project_service.dtos.mapper;

import com.sketchnotes.project_service.dtos.request.ProjectRequest;
import com.sketchnotes.project_service.dtos.response.ProjectResponse;
import com.sketchnotes.project_service.entity.Project;

public class ProjectMapper {
    public static ProjectResponse toDTO(Project project) {
            if(project.getPages() == null) {
                return ProjectResponse.builder()
                        .projectId(project.getProjectId())
                        .name(project.getName())
                        .description(project.getDescription())
                        .ownerId(project.getOwnerId())
                        .imageUrl(project.getImageUrl())
                        .pages(null)
                        .build();
            }
        return ProjectResponse.builder()
                .projectId(project.getProjectId())
                .name(project.getName())
                .description(project.getDescription())
                .ownerId(project.getOwnerId())
                .pages(project.getPages().stream()
                        .map(PageMapper::toDTO)
                        .toList())
                .build();
    }

    public static Project toEntity(ProjectRequest dto) {
        return Project.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .build();
    }
}

