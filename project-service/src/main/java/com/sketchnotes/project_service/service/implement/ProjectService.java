package com.sketchnotes.project_service.service.implement;

import com.sketchnotes.project_service.client.IUserClient;
import com.sketchnotes.project_service.client.IdentityServiceClient;
import com.sketchnotes.project_service.dtos.ApiResponse;
import com.sketchnotes.project_service.dtos.request.ProjectRequest;
import com.sketchnotes.project_service.dtos.response.ProjectListResponse;
import com.sketchnotes.project_service.dtos.response.ProjectResponse;
import com.sketchnotes.project_service.dtos.response.ProjectDetailResponse;
import com.sketchnotes.project_service.dtos.mapper.ProjectMapper;
import com.sketchnotes.project_service.dtos.response.UserQuotaResponse;
import com.sketchnotes.project_service.dtos.response.UserResponse;
import com.sketchnotes.project_service.entity.Project;
import com.sketchnotes.project_service.entity.ProjectCollaboration;
import com.sketchnotes.project_service.exception.AppException;
import com.sketchnotes.project_service.exception.ErrorCode;
import com.sketchnotes.project_service.repository.IProjectCollaborationRepository;
import com.sketchnotes.project_service.repository.IProjectRepository;
import com.sketchnotes.project_service.service.IProjectService;
import com.sketchnotes.project_service.utils.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService implements IProjectService {
    private final IProjectRepository projectRepository;
    private final IUserClient userClient;
    private final IProjectCollaborationRepository projectCollaborationRepository;
    private final IdentityServiceClient identityServiceClient;

    @Override
    @CacheEvict(value = "projects",  key ="'user' +#ownerId")
    public ProjectResponse createProject(ProjectRequest dto, Long ownerId) {
        ApiResponse<UserResponse>  user = userClient.getCurrentUser();

        // Check project quota before creating
        try {
            UserQuotaResponse quota = identityServiceClient.getUserQuota(user.getResult().getId());
            if (!quota.getCanCreateProject()) {
                throw new AppException(ErrorCode.PROJECT_QUOTA_EXCEEDED);
            }
        } catch (Exception e) {
            // If identity-service is down, log error but allow creation (fail-open)
            // In production, you might want to fail-closed instead
            System.err.println("Failed to check quota: " + e.getMessage());
        }

        Project project = Project.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .ownerId(user.getResult().getId())
                .imageUrl(dto.getImageUrl())
                .build();
        Project saved = projectRepository.save(project);
        return ProjectMapper.toDTO(saved);
    }
    @Override
    public ProjectDetailResponse getProject(Long id) {
        Project project = projectRepository.findById(id).filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
        boolean hasCollaboration = projectCollaborationRepository.existsByProjectAndDeletedAtIsNull(project);
        Long userId = userClient.getCurrentUser().getResult().getId();
        boolean isEdited =  projectCollaborationRepository.existsByProjectAndIsEditedTrueAndDeletedAtIsNull(project);
        boolean isOwner = project.getOwnerId().equals(userId);
        if(isOwner){
            isEdited = true;
        }
        return ProjectMapper.toDetailDTO(project, hasCollaboration,isEdited,isOwner);
    }

    @Override
   /// @Cacheable(value = "projects", key ="'user' +#ownerId")
    public ProjectListResponse getProjectsByOwner(Long ownerId) {
        List<Project> projects = projectRepository.findByOwnerIdAndDeletedAtIsNullOrderByCreatedAtDesc(ownerId);
        if (projects.isEmpty()) {
            return new ProjectListResponse(Collections.emptyList());
        }
        return new ProjectListResponse(projects.stream()
                .map(ProjectMapper::toDTO)
                .toList());
    }
    @Override
    ///@Cacheable(value = "projects", key ="'user' +#ownerId")
    public ProjectListResponse getProjectsCurrentUser(Long ownerId) {
        ApiResponse<UserResponse> user = userClient.getCurrentUser();
        List<Project> projects = projectRepository.findByOwnerIdAndDeletedAtIsNullOrderByCreatedAtDesc(user.getResult().getId());
        if (projects.isEmpty()) {
            return new ProjectListResponse(Collections.emptyList());
        }
        return  new ProjectListResponse(projects.stream()
                .map(ProjectMapper::toDTO)
                .toList());
    }

    @Override
    public PagedResponse<ProjectResponse> getProjectsCurrentUserPaged(Long ownerId, int pageNo, int pageSize) {
        ApiResponse<UserResponse> user = userClient.getCurrentUser();
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Project> projectPage = projectRepository.findByOwnerIdAndDeletedAtIsNull(user.getResult().getId(), pageable);
        
        List<ProjectResponse> content = projectPage.getContent().stream()
                .map(ProjectMapper::toDTO)
                .toList();
        
        return new PagedResponse<>(
                content,
                projectPage.getNumber(),
                projectPage.getSize(),
                projectPage.getTotalElements(),
                projectPage.getTotalPages(),
                projectPage.isLast()
        );
    }

    @Override

    public ProjectListResponse getSharedProjectsCurrentUser() {
        ApiResponse<UserResponse> user = userClient.getCurrentUser();
        List<ProjectCollaboration> projects = projectCollaborationRepository.findByUserIdAndDeletedAtIsNull(user.getResult().getId());
        if (projects.isEmpty()) {
            return new ProjectListResponse(Collections.emptyList());
        }
        return  new ProjectListResponse(projects.stream()
                .map(pc -> {
                    Project project = pc.getProject();
                    boolean isEdited = pc.isEdited();
                    return ProjectMapper.toCollabProjectDTO(project, isEdited);
                })
                .toList());
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "projects",  key ="'user' +#ownerId"),
            @CacheEvict(value = "projects", key = "#id")
    })
    public ProjectResponse updateProject(Long id, ProjectRequest dto, Long ownerId) {
        Project project = projectRepository.findById(id).filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        Project updated = projectRepository.save(project);
        return ProjectMapper.toDTO(updated);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "projects",  key ="'user' +#ownerId"),
            @CacheEvict(value = "projects", key = "#id")
    })
    public void deleteProject(Long id, Long ownerId) {
        Project project = projectRepository.findById(id).filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
        project.setDeletedAt(LocalDateTime.now());
        projectRepository.save(project);
    }

    @Override
    public Long getProjectCountByOwner(Long ownerId) {
        return projectRepository.countByOwnerIdAndDeletedAtIsNull(ownerId);
    }
}

