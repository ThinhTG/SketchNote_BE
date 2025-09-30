package com.sketchnotes.project_service.service;

import com.sketchnotes.project_service.dtos.PageDTO;

import java.util.List;

public interface PageService {
    PageDTO addPage(Long projectId, PageDTO dto);
    List<PageDTO> getPagesByProject(Long projectId);
    PageDTO updatePage(Long pageId, PageDTO dto);
    void deletePage(Long pageId);
}
