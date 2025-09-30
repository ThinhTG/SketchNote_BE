package com.sketchnotes.project_service.dtos.mapper;

import com.sketchnotes.project_service.dtos.ProjectDTO;
import com.sketchnotes.project_service.entity.Project;

public class ProjectMapper {
    public static ProjectDTO toDTO(Project project) {
        return ProjectDTO.builder()
                .projectId(project.getProjectId())
                .name(project.getName())
                .description(project.getDescription())
                .ownerId(project.getOwnerId())
                .pages(project.getPages().stream()
                        .map(PageMapper::toDTO)
                        .toList())
                .build();
    }

    public static Project toEntity(ProjectDTO dto) {
        return Project.builder()
                .projectId(dto.getProjectId())
                .name(dto.getName())
                .description(dto.getDescription())
                .ownerId(dto.getOwnerId())
                .build();
    }
}

