package com.sketchnotes.project_service.service.implement;

import com.sketchnotes.project_service.client.IUserClient;
import com.sketchnotes.project_service.dtos.ApiResponse;
import com.sketchnotes.project_service.dtos.request.AcceptanceRequest;
import com.sketchnotes.project_service.dtos.request.CollabRequest;
import com.sketchnotes.project_service.dtos.response.ProjectCollaborationResponse;
import com.sketchnotes.project_service.dtos.response.UserResponse;
import com.sketchnotes.project_service.entity.Project;
import com.sketchnotes.project_service.entity.ProjectCollaboration;
import com.sketchnotes.project_service.enums.NotificationType;
import com.sketchnotes.project_service.events.NotificationEvent;
import com.sketchnotes.project_service.exception.AppException;
import com.sketchnotes.project_service.exception.ErrorCode;
import com.sketchnotes.project_service.producer.NotificationProducer;
import com.sketchnotes.project_service.repository.IProjectCollaborationRepository;
import com.sketchnotes.project_service.repository.IProjectRepository;
import com.sketchnotes.project_service.service.IProjectCollaborationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectCollaborationService implements IProjectCollaborationService {
    private final IProjectCollaborationRepository projectCollaborationRepository;
    private final IProjectRepository projectRepository;
    private final IUserClient userClient;
    private final NotificationProducer notificationProducer;

    @Override
    public void inviteUserToProject(CollabRequest dto) {
        Project project = projectRepository.findById(dto.getProjectId()).filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
        ApiResponse<UserResponse> user = userClient.getCurrentUser();
        if(!project.getOwnerId().equals(user.getResult().getId())) {
            throw new AppException(ErrorCode.FORBIDDEN_ACTION);
        }
        ProjectCollaboration collab = projectCollaborationRepository.findByProjectAndUserIdAndDeletedAtIsNull(
                        project, dto.getUserId())
                .orElse(null);
        if (collab != null) {
            throw new AppException(ErrorCode.USER_ALREADY_COLLABORATOR);
        }
        ApiResponse<UserResponse> collabUser = userClient.getUserById(dto.getUserId());
        if(collabUser == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        ProjectCollaboration projectCollaboration = new ProjectCollaboration();
        projectCollaboration.setProject(project);
        projectCollaboration.setAccepted(false);
        projectCollaboration.setUserId(collabUser.getResult().getId());
        projectCollaboration.setEdited(dto.isEdited());
        projectCollaborationRepository.save(projectCollaboration);

        // Send notification event to Kafka
        NotificationEvent notificationEvent = NotificationEvent.builder()
                .userId(collabUser.getResult().getId())
                .title("Project Collaboration Invitation")
                .message(String.format("You have been invited to collaborate on project: %s",
                        project.getName()))
                .type(NotificationType.COLLABORATION.toString())
                .projectId(project.getProjectId())
                .build();
        
        notificationProducer.sendNotificationEvent(notificationEvent);
    }

    @Override
    public void removeUserFromProject(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId).filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
        ApiResponse<UserResponse> user = userClient.getCurrentUser();
        if(!project.getOwnerId().equals(user.getResult().getId())) {
            throw new AppException(ErrorCode.FORBIDDEN_ACTION);
        }
        ProjectCollaboration projectCollaboration = projectCollaborationRepository.findByProjectAndUserIdAndDeletedAtIsNull(
                        project, user.getResult().getId())
                .orElseThrow(() -> new AppException(ErrorCode.COLLAB_NOT_FOUND));
        projectCollaboration.setDeletedAt(LocalDateTime.now());
        projectCollaborationRepository.save(projectCollaboration);
    }

    @Override
    public void changeUserPermission(CollabRequest dto) {
        Project project = projectRepository.findById(dto.getProjectId()).filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
        ApiResponse<UserResponse> user = userClient.getCurrentUser();
        if(!project.getOwnerId().equals(user.getResult().getId())) {
            throw new AppException(ErrorCode.FORBIDDEN_ACTION);
        }
        ProjectCollaboration projectCollaboration = projectCollaborationRepository.findByProjectAndUserIdAndDeletedAtIsNull(
                        project, dto.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.COLLAB_NOT_FOUND));
        projectCollaboration.setProject(project);
        projectCollaboration.setUserId(user.getResult().getId());
        projectCollaboration.setEdited(dto.isEdited());
        projectCollaborationRepository.save(projectCollaboration);
    }

    @Override
    public List<ProjectCollaborationResponse> listProjectCollaborators(Long projectId) {
        Project project = projectRepository.findById(projectId).filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
        List<ProjectCollaboration> projectCollaborations = projectCollaborationRepository.findByProjectAndDeletedAtIsNull(project);
        List<ProjectCollaborationResponse>  listResponse= new ArrayList<>(projectCollaborations.stream().map(p -> ProjectCollaborationResponse.builder()
                .projectId(p.getProject().getProjectId())
                .email(userClient.getUserById(p.getUserId()).getResult().getEmail())
                .userId(p.getUserId())
                .isEdited(p.isEdited())
                .avatarUrl(userClient.getUserById(p.getUserId()).getResult().getAvatarUrl())
                .createdAt(p.getCreatedAt())
                .build()).toList());

        listResponse.add(ProjectCollaborationResponse.builder()
                .projectId(project.getProjectId())
                .email(userClient.getUserById(project.getOwnerId()).getResult().getEmail())
                .userId(project.getOwnerId())
                .isEdited(true)
                .avatarUrl(userClient.getUserById(project.getOwnerId()).getResult().getAvatarUrl())
                .createdAt(project.getCreatedAt())
                .build());
        return listResponse;
    }

    @Override
    public void acceptProjectInvitation(AcceptanceRequest request) {
        Project project = projectRepository.findById(request.getProjectId()).filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
        ApiResponse<UserResponse> user = userClient.getCurrentUser();
        ProjectCollaboration projectCollaboration = projectCollaborationRepository.findByProjectAndUserIdAndDeletedAtIsNull(
                        project, user.getResult().getId())
                .orElseThrow(() -> new AppException(ErrorCode.COLLAB_NOT_FOUND));
        if (request.isAccepted()) {
            projectCollaboration.setAccepted(true);
        }else {
            projectCollaboration.setDeletedAt(LocalDateTime.now());
        }
        projectCollaborationRepository.save(projectCollaboration);
    }
}
