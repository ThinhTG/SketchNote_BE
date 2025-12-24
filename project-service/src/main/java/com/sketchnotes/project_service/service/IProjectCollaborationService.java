package com.sketchnotes.project_service.service;

import com.sketchnotes.project_service.dtos.request.AcceptanceRequest;
import com.sketchnotes.project_service.dtos.request.CollabRequest;
import com.sketchnotes.project_service.dtos.response.ProjectCollaborationResponse;

import java.util.List;

public interface IProjectCollaborationService {
     void inviteUserToProject(CollabRequest dto);
     void removeUserFromProject(Long projectId, Long userId);
     void changeUserPermission(CollabRequest dto);
     List<ProjectCollaborationResponse> listProjectCollaborators(Long projectId);
      void acceptProjectInvitation(AcceptanceRequest request);
}
