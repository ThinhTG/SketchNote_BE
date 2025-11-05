package com.sketchnotes.project_service.service;

import com.sketchnotes.project_service.dtos.request.ListPageRequest;
import com.sketchnotes.project_service.dtos.request.PageRequest;
import com.sketchnotes.project_service.dtos.response.PageResponse;
import com.sketchnotes.project_service.dtos.request.UpdatePageRequest;

import java.util.List;

public interface IPageService {
    List<PageResponse> addPages(ListPageRequest dtos);
    PageResponse addPage(PageRequest dto);
    List<PageResponse> getPagesByProject(Long projectId);
    PageResponse updatePage(Long pageId, UpdatePageRequest dto);
    void deletePage(Long pageId);
}
