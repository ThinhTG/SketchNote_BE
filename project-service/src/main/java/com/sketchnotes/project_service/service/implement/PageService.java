package com.sketchnotes.project_service.service.implement;

import com.sketchnotes.project_service.dtos.request.PageRequest;
import com.sketchnotes.project_service.dtos.response.PageResponse;
import com.sketchnotes.project_service.dtos.mapper.PageMapper;
import com.sketchnotes.project_service.dtos.request.UpdatePageRequest;
import com.sketchnotes.project_service.entity.Page;
import com.sketchnotes.project_service.entity.Project;
import com.sketchnotes.project_service.exception.AppException;
import com.sketchnotes.project_service.exception.ErrorCode;
import com.sketchnotes.project_service.repository.IPageRepository;
import com.sketchnotes.project_service.repository.IProjectRepository;
import com.sketchnotes.project_service.service.IPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PageService implements IPageService {
    private final IProjectRepository projectRepository;
    private final IPageRepository pageRepository;

    @Override
    public PageResponse addPage(PageRequest dto) {
        Project project = projectRepository.findById(dto.getProjectId()).filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
        Page page = PageMapper.toEntity(dto, project);
        Page saved = pageRepository.save(page);
        return PageMapper.toDTO(saved);
    }

    @Override
    public List<PageResponse> getPagesByProject(Long projectId) {
        List<Page> pages = pageRepository.findByProject_ProjectIdAndDeletedAtIsNullOrderByPageNumberAsc(projectId);
        if (pages.isEmpty()) {
            return null;
        }
        return pages.stream().map(PageMapper::toDTO) .toList();
    }

    @Override
    public PageResponse updatePage(Long pageId, UpdatePageRequest dto) {
        Page page = pageRepository.findById(pageId).filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new AppException(ErrorCode.PAGE_NOT_FOUND));
        page.setPageNumber(dto.getPageNumber());
        page.setStrokeUrl(dto.getStrokeUrl());
        Page updated = pageRepository.save(page);
        return PageMapper.toDTO(updated);
    }

    @Override
    public void deletePage(Long pageId) {
        Page page = pageRepository.findById(pageId).filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new AppException(ErrorCode.PAGE_NOT_FOUND));
        page.setDeletedAt(LocalDateTime.now());
        pageRepository.save(page);
    }
}
