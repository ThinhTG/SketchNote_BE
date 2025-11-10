package com.sketchnotes.project_service.service.implement;

import com.sketchnotes.project_service.dtos.request.ListPageRequest;
import com.sketchnotes.project_service.dtos.request.PageDto;
import com.sketchnotes.project_service.dtos.request.PageRequest;
import com.sketchnotes.project_service.dtos.response.PageResponse;
import com.sketchnotes.project_service.dtos.mapper.PageMapper;
import com.sketchnotes.project_service.dtos.request.UpdatePageRequest;
import com.sketchnotes.project_service.entity.Page;
import com.sketchnotes.project_service.entity.Project;
import com.sketchnotes.project_service.entity.ProjectVersion;
import com.sketchnotes.project_service.exception.AppException;
import com.sketchnotes.project_service.exception.ErrorCode;
import com.sketchnotes.project_service.repository.IPageRepository;
import com.sketchnotes.project_service.repository.IProjectRepository;
import com.sketchnotes.project_service.repository.IProjectVersionRepository;
import com.sketchnotes.project_service.service.IPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PageService implements IPageService {
    private final IProjectRepository projectRepository;
    private final IPageRepository pageRepository;
    private  final IProjectVersionRepository projectVersionRepository;
    private final ProjectVersionService projectVersionService;

    @Override
    @Caching(evict = {
            @CacheEvict(value = "projects",  key ="'user' +#ownerId"),
            @CacheEvict(value = "projects", key = "#dtos.projectId")
    })
    public List<PageResponse> addPages(ListPageRequest dtos, Long ownerId) {
        Project project = projectRepository.findById(dtos.getProjectId()).filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
        ProjectVersion lastVersion = project.getProjectVersions().stream()
                .max(Comparator.comparing(ProjectVersion::getVersionNumber))
                .orElse(null);

        // Soft delete old pages before creating new version
        if(project.getPages() != null) {
            for (Page page : project.getPages()) {

                    page.setProject(null);
                    pageRepository.save(page);

            }
        }

        ProjectVersion version = new ProjectVersion();
        if (lastVersion == null) {
            version.setVersionNumber(1L);
        } else {
            version.setVersionNumber(lastVersion.getVersionNumber() + 1);
        }
        version.setCreatedAt(LocalDateTime.now());
        version.setNote(project.getName() + " - Version " + version.getVersionNumber());
        version.setProject(project);
        project.getProjectVersions().add(version);
        projectRepository.save(project);
        projectVersionRepository.save(version);
        for (PageDto dto : dtos.getPages()) {
            Page page = PageMapper.toEntity(dto, project);
            page.setProjectVersion(version);
            pageRepository.save(page);
        }
        
        // Clean up old versions if more than 10 exist
        projectVersionService.cleanupOldVersions(dtos.getProjectId());
        
        return getPagesByProject(dtos.getProjectId());
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "projects",  key ="'user' +#ownerId"),
            @CacheEvict(value = "projects", key = "#id")
    })
    public PageResponse addPage(PageRequest dto, Long ownerId) {
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
    @CacheEvict(value = "projects", allEntries = true)
    public PageResponse updatePage(Long pageId, UpdatePageRequest dto) {
        Page page = pageRepository.findById(pageId).filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new AppException(ErrorCode.PAGE_NOT_FOUND));
        page.setPageNumber(dto.getPageNumber());
        page.setStrokeUrl(dto.getStrokeUrl());
        Page updated = pageRepository.save(page);
        return PageMapper.toDTO(updated);
    }

    @Override
    @CacheEvict(value = "projects", allEntries = true)
    public void deletePage(Long pageId) {
        Page page = pageRepository.findById(pageId).filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new AppException(ErrorCode.PAGE_NOT_FOUND));
        page.setDeletedAt(LocalDateTime.now());
        pageRepository.save(page);
    }
}
