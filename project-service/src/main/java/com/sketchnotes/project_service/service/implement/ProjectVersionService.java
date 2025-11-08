package com.sketchnotes.project_service.service.implement;

import com.sketchnotes.project_service.dtos.mapper.ProjectVersionMapper;
import com.sketchnotes.project_service.dtos.response.ProjectVersionResponse;
import com.sketchnotes.project_service.entity.Page;
import com.sketchnotes.project_service.entity.Project;
import com.sketchnotes.project_service.entity.ProjectVersion;
import com.sketchnotes.project_service.exception.AppException;
import com.sketchnotes.project_service.exception.ErrorCode;
import com.sketchnotes.project_service.repository.IPageRepository;
import com.sketchnotes.project_service.repository.IProjectRepository;
import com.sketchnotes.project_service.repository.IProjectVersionRepository;
import com.sketchnotes.project_service.service.IProjectVersionService;
import com.sketchnotes.project_service.service.IStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectVersionService implements IProjectVersionService {
    private final IProjectRepository projectRepository;
    private final IProjectVersionRepository projectVersionRepository;
    private final IPageRepository pageRepository;
    private final IStorageService storageService;
    
    private static final int MAX_VERSIONS = 10;

    @Override
    public List<ProjectVersionResponse> getRecentVersions(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
        List<ProjectVersion> versions = projectVersionRepository
                .findByProjectAndDeletedAtIsNullOrderByCreatedAtDesc(project);
        
        return versions.stream()
                .limit(MAX_VERSIONS)
                .map(ProjectVersionMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "projects", key = "#projectId")
    public void restoreVersion(Long projectId, Long versionId) {
        // Validate project exists
        Project project = projectRepository.findById(projectId)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
        
        // Validate version exists and belongs to project
        ProjectVersion selectedVersion = projectVersionRepository.findById(versionId)
                .filter(v -> v.getDeletedAt() == null)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
        
        if (!selectedVersion.getProject().getProjectId().equals(projectId)) {
            throw new AppException(ErrorCode.PROJECT_NOT_FOUND);
        }
        
        // Remove current pages from project (set project to null)
        List<Page> currentPages = pageRepository
                .findByProject_ProjectIdAndDeletedAtIsNullOrderByPageNumberAsc(projectId);
        for (Page page : currentPages) {
            page.setProject(null);
            pageRepository.save(page);
        }
        
        // Clone pages from selected version and assign to project
        List<Page> versionPages = selectedVersion.getPages().stream()
                .filter(p -> p.getDeletedAt() == null)
                .collect(Collectors.toList());
        
        // Get the latest version number to create new version
        ProjectVersion lastVersion = project.getProjectVersions().stream()
                .filter(v -> v.getDeletedAt() == null)
                .max(Comparator.comparing(ProjectVersion::getVersionNumber))
                .orElse(null);
        
        // Create new version for this restoration
        ProjectVersion newVersion = new ProjectVersion();
        newVersion.setVersionNumber(lastVersion != null ? lastVersion.getVersionNumber() + 1 : 1L);
        newVersion.setCreatedAt(LocalDateTime.now());
        newVersion.setNote("Restored from version " + selectedVersion.getVersionNumber());
        newVersion.setProject(project);
        projectVersionRepository.save(newVersion);
        
        // Clone pages from selected version
        for (Page oldPage : versionPages) {
            Page newPage = Page.builder()
                    .project(project)
                    .projectVersion(newVersion)
                    .pageNumber(oldPage.getPageNumber())
                    .strokeUrl(oldPage.getStrokeUrl())
                    .build();
            pageRepository.save(newPage);
        }
        
        // Check if we need to delete old versions (keep only 10 most recent)
        deleteOldVersionsIfNeeded(project);
        
        projectRepository.save(project);
    }
    
    /**
     * Delete oldest versions if count exceeds MAX_VERSIONS
     */
    private void deleteOldVersionsIfNeeded(Project project) {
        List<ProjectVersion> allVersions = projectVersionRepository
                .findByProjectAndDeletedAtIsNullOrderByCreatedAtDesc(project);
        
        if (allVersions.size() > MAX_VERSIONS) {
            // Get versions to delete (oldest ones)
            List<ProjectVersion> versionsToDelete = allVersions.stream()
                    .skip(MAX_VERSIONS)
                    .collect(Collectors.toList());
            
            for (ProjectVersion version : versionsToDelete) {
                // Soft delete the version
                version.setDeletedAt(LocalDateTime.now());
                projectVersionRepository.save(version);
                
                // Soft delete associated pages and delete files from S3
                for (Page page : version.getPages()) {
                    if (page.getDeletedAt() == null) {
                        // Delete file from S3
                        if (page.getStrokeUrl() != null && !page.getStrokeUrl().isEmpty()) {
                            storageService.deleteFile(page.getStrokeUrl());
                        }
                        
                        // Soft delete the page
                        page.setDeletedAt(LocalDateTime.now());
                        pageRepository.save(page);
                    }
                }
            }
        }
    }
    
    /**
     * Public method to clean up old versions when creating new ones
     * This should be called from PageService when creating new versions
     */
    @Transactional
    public void cleanupOldVersions(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
        
        deleteOldVersionsIfNeeded(project);
    }
}
