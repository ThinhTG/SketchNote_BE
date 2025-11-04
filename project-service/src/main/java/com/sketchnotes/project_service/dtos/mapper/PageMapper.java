package com.sketchnotes.project_service.dtos.mapper;

import com.sketchnotes.project_service.dtos.request.PageDto;
import com.sketchnotes.project_service.dtos.request.PageRequest;
import com.sketchnotes.project_service.dtos.response.PageResponse;
import com.sketchnotes.project_service.entity.Page;
import com.sketchnotes.project_service.entity.Project;

public class PageMapper {
    public static PageResponse toDTO(Page page) {
        return PageResponse.builder()
                .pageId(page.getPageId())
                .projectId(page.getProject().getProjectId())
                .pageNumber(page.getPageNumber())
                .strokeUrl(page.getStrokeUrl())
                .build();
    }

    public static Page toEntity(PageRequest dto, Project project) {
        return Page.builder()
                .pageNumber(dto.getPageNumber())
                .strokeUrl(dto.getStrokeUrl())
                .project(project)
                .build();
    }
    public static Page toEntity(PageDto dto, Project project) {
        return Page.builder()
                .pageNumber(dto.getPageNumber())
                .strokeUrl(dto.getStrokeUrl())
                .project(project)
                .build();
    }
}
