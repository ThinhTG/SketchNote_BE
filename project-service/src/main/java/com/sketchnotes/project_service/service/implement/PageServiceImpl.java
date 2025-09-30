package com.sketchnotes.project_service.service.implement;

import com.sketchnotes.project_service.dtos.PageDTO;
import com.sketchnotes.project_service.dtos.mapper.PageMapper;
import com.sketchnotes.project_service.entity.Page;
import com.sketchnotes.project_service.entity.Project;
import com.sketchnotes.project_service.repository.PageRepository;
import com.sketchnotes.project_service.repository.ProjectRepository;
import com.sketchnotes.project_service.service.PageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PageServiceImpl implements PageService {
    private final ProjectRepository projectRepository;
    private final PageRepository pageRepository;

    @Override
    public PageDTO addPage(Long projectId, PageDTO dto) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        Page page = PageMapper.toEntity(dto, project);
        Page saved = pageRepository.save(page);
        return PageMapper.toDTO(saved);
    }

    @Override
    public List<PageDTO> getPagesByProject(Long projectId) {
        return pageRepository.findByProject_ProjectId(projectId).stream()
                .map(PageMapper::toDTO)
                .toList();
    }

    @Override
    public PageDTO updatePage(Long pageId, PageDTO dto) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new RuntimeException("Page not found"));
        page.setPageNumber(dto.getPageNumber());
        page.setStrokeUrl(dto.getStrokeUrl());
        Page updated = pageRepository.save(page);
        return PageMapper.toDTO(updated);
    }

    @Override
    public void deletePage(Long pageId) {
        pageRepository.deleteById(pageId);
    }
}
