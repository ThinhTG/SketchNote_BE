package com.sketchnotes.project_service.service.implement;

import com.sketchnotes.project_service.client.IUserClient;
import com.sketchnotes.project_service.dtos.ApiResponse;
import com.sketchnotes.project_service.dtos.request.ProjectRequest;
import com.sketchnotes.project_service.dtos.response.ProjectResponse;
import com.sketchnotes.project_service.dtos.mapper.ProjectMapper;
import com.sketchnotes.project_service.dtos.response.UserResponse;
import com.sketchnotes.project_service.entity.Project;
import com.sketchnotes.project_service.exception.AppException;
import com.sketchnotes.project_service.exception.ErrorCode;
import com.sketchnotes.project_service.repository.IProjectRepository;
import com.sketchnotes.project_service.service.IProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService implements IProjectService {
    private final IProjectRepository projectRepository;
    private final IUserClient userClient;

    @Override
    @CacheEvict(value = "projects", allEntries = true)
    public ProjectResponse createProject(ProjectRequest dto) {
        ApiResponse<UserResponse>  user = userClient.getCurrentUser();

        Project project = Project.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .ownerId(user.getResult().getId())
                .build();
        Project saved = projectRepository.save(project);
        return ProjectMapper.toDTO(saved);
    }

    @Override
    @Cacheable(value = "projects", key = "#id")
    public ProjectResponse getProject(Long id) {
        Project project = projectRepository.findById(id).filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
        return ProjectMapper.toDTO(project);
    }

    @Override
    @Cacheable(value = "projects", key = "#ownerId")
    public List<ProjectResponse> getProjectsByOwner(Long ownerId) {
        List<Project> projects = projectRepository.findByOwnerIdAndDeletedAtIsNullOrderByCreatedAtDesc(ownerId);
        if (projects.isEmpty()) {
            return null;
        }
        return projects.stream()
                .map(ProjectMapper::toDTO)
                .toList();
    }
    @Override
    public List<ProjectResponse> getProjectsCurrentUser() {
        ApiResponse<UserResponse> user = userClient.getCurrentUser();
        List<Project> projects = projectRepository.findByOwnerIdAndDeletedAtIsNullOrderByCreatedAtDesc(user.getResult().getId());
        if (projects.isEmpty()) {
            return null;
        }
        return projects.stream()
                .map(ProjectMapper::toDTO)
                .toList();
    }

    @Override
    @CacheEvict(value = "projects", allEntries = true)
    public ProjectResponse updateProject(Long id, ProjectRequest dto) {
        Project project = projectRepository.findById(id).filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        Project updated = projectRepository.save(project);
        return ProjectMapper.toDTO(updated);
    }

    @Override
    @CacheEvict(value = "projects", allEntries = true)
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id).filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
        project.setDeletedAt(LocalDateTime.now());
        projectRepository.save(project);
    }
}

