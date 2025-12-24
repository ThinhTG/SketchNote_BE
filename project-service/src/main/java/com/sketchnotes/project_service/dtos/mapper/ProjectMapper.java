package com.sketchnotes.project_service.dtos.mapper;

import com.sketchnotes.project_service.dtos.request.ProjectRequest;
import com.sketchnotes.project_service.dtos.response.ProjectResponse;
import com.sketchnotes.project_service.dtos.response.ProjectDetailResponse;
import com.sketchnotes.project_service.entity.Project;
import com.sketchnotes.project_service.enums.PaperSize;

public class ProjectMapper {
    public static ProjectResponse toDTO(Project project) {
            if(project.getPages() == null || project.getPages().isEmpty()) {
                return ProjectResponse.builder()
                        .projectId(project.getProjectId())
                        .name(project.getName())
                        .description(project.getDescription())
                        .ownerId(project.getOwnerId())
                        .paperSize(project.getPaperSize() == null ? null : project.getPaperSize().toString())
                        .imageUrl(project.getImageUrl())
                        .isEdited(true)
                        .isOwner(true)
                        .pages(null)
                        .build();
            }
        
        // Filter: only pages that are NOT deleted AND NOT assigned to a version (current working pages)
        var activePages = project.getPages().stream()
                .filter(page -> page.getDeletedAt() == null)  // Not soft deleted
                .map(PageMapper::toDTO)
                .toList();
        
        return ProjectResponse.builder()
                .projectId(project.getProjectId())
                .name(project.getName())
                .description(project.getDescription())
                .ownerId(project.getOwnerId())
                .imageUrl(project.getImageUrl())
                .paperSize(project.getPaperSize() == null ? null : project.getPaperSize().toString())
                .isEdited(true)
                .isOwner(true)
                .pages(activePages)
                .build();
    }

    public static Project toEntity(ProjectRequest dto) {
        return Project.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .paperSize(PaperSize.valueOf(dto.getPaperSize()))
                .imageUrl(dto.getImageUrl())
                .build();
    }

    public static ProjectResponse toCollabProjectDTO(Project project, boolean isEdited,boolean isAccepted) {
        if(project.getPages() == null || project.getPages().isEmpty()) {
            return ProjectResponse.builder()
                    .projectId(project.getProjectId())
                    .name(project.getName())
                    .description(project.getDescription())
                    .paperSize(project.getPaperSize() == null ? null : project.getPaperSize().toString())
                    .ownerId(project.getOwnerId())
                    .imageUrl(project.getImageUrl())
                    .isAccepted(isAccepted)
                    .isEdited(isEdited)
                    .isOwner(false)
                    .pages(null)
                    .build();
        }
        
        // Filter: only pages that are NOT deleted AND NOT assigned to a version
        var activePages = project.getPages().stream()
                .filter(page -> page.getDeletedAt() == null)
                .map(PageMapper::toDTO)
                .toList();
        
        return ProjectResponse.builder()
                .projectId(project.getProjectId())
                .name(project.getName())
                .description(project.getDescription())
                .ownerId(project.getOwnerId())
                .imageUrl(project.getImageUrl())
                .paperSize(project.getPaperSize() == null ? null : project.getPaperSize().toString())
                .isEdited(isEdited)
                .isOwner(false)
                .isAccepted(isAccepted)
                .pages(activePages)
                .build();
    }

    public static ProjectDetailResponse toDetailDTO(Project project, boolean hasCollaboration,boolean isEdited, boolean isOwner) {
        if(project.getPages() == null || project.getPages().isEmpty()) {
            return ProjectDetailResponse.builder()
                    .projectId(project.getProjectId())
                    .name(project.getName())
                    .description(project.getDescription())
                    .ownerId(project.getOwnerId())
                    .paperSize(project.getPaperSize() == null ? null : project.getPaperSize().toString())
                    .imageUrl(project.getImageUrl())
                    .isEdited(isEdited)
                    .isOwner(isOwner)
                    .hasCollaboration(hasCollaboration)
                    .pages(null)
                    .build();
        }

        // Filter: only pages that are NOT deleted AND NOT assigned to a version (current working pages)
        var activePages = project.getPages().stream()
                .filter(page -> page.getDeletedAt() == null)  // Not soft deleted
                .map(PageMapper::toDTO)
                .toList();

        return ProjectDetailResponse.builder()
                .projectId(project.getProjectId())
                .name(project.getName())
                .description(project.getDescription())
                .ownerId(project.getOwnerId())
                .imageUrl(project.getImageUrl())
                .paperSize(project.getPaperSize() == null ? null : project.getPaperSize().toString())
                .isEdited(isEdited)
                .isOwner(isOwner)
                .hasCollaboration(hasCollaboration)
                .pages(activePages)
                .build();
    }
}

