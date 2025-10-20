package com.sketchnotes.project_service.service;

import com.sketchnotes.project_service.dtos.request.CollbabRequest;

public interface IProjectCollaborationService {
    public void inviteUserToProject(CollbabRequest dto);
    public void removeUserFromProject(CollbabRequest dto);
}
