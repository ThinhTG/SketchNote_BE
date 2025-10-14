package com.sketchnotes.project_service.service;

import com.sketchnotes.project_service.dtos.request.ProjectRequest;
import com.sketchnotes.project_service.dtos.response.ProjectResponse;

import java.util.List;

public interface IProjectService {
    ProjectResponse createProject(ProjectRequest dto);
    ProjectResponse getProject(Long id);
    List<ProjectResponse> getProjectsByOwner(Long ownerId);
    List<ProjectResponse> getProjectsCurrentUser();
    ProjectResponse updateProject(Long id, ProjectRequest dto);
    void deleteProject(Long id);
}

