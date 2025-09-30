package com.sketchnotes.project_service.dtos.mapper;

import com.sketchnotes.project_service.dtos.PageDTO;
import com.sketchnotes.project_service.entity.Page;
import com.sketchnotes.project_service.entity.Project;

public class PageMapper {
    public static PageDTO toDTO(Page page) {
        return PageDTO.builder()
                .pageId(page.getPageId())
                .pageNumber(page.getPageNumber())
                .strokeUrl(page.getStrokeUrl())
                .build();
    }

    public static Page toEntity(PageDTO dto, Project project) {
        return Page.builder()
                .pageId(dto.getPageId())
                .pageNumber(dto.getPageNumber())
                .strokeUrl(dto.getStrokeUrl())
                .project(project)
                .build();
    }
}
