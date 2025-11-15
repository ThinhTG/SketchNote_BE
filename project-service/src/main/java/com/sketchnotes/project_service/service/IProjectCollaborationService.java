package com.sketchnotes.project_service.service;

import com.sketchnotes.project_service.dtos.request.CollabRequest;
import com.sketchnotes.project_service.dtos.response.ProjectCollaborationResponse;

import java.util.List;

public interface IProjectCollaborationService {
    public void inviteUserToProject(CollabRequest dto);
    public void removeUserFromProject(Long projectId, Long userId);
    public void changeUserPermission(CollabRequest dto);
    public List<ProjectCollaborationResponse> listProjectCollaborators(Long projectId);
}
